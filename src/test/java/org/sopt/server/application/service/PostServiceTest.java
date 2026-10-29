package org.sopt.server.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sopt.server.application.port.out.MemberRepository;
import org.sopt.server.domain.Category;
import org.sopt.server.domain.Member;
import org.sopt.server.domain.Post;
import org.sopt.server.domain.exception.BaseException;
import org.sopt.server.domain.exception.MemberErrorCode;
import org.sopt.server.domain.exception.PostErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class PostServiceTest {
  private static final long NOT_EXIST_ID = Long.MAX_VALUE;

  @Autowired private PostService postService;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private MemberRepository memberRepository;

  private long memberId;

  @BeforeEach
  void setUp() {
    memberId = memberRepository.save(new Member("작성자")).getId();
  }

  @AfterEach
  void tearDown() {
    // 소프트 삭제된 데이터는 리포지토리로 조회되지 않으므로 테이블을 직접 비운다
    jdbcTemplate.update("delete from post");
    jdbcTemplate.update("delete from member");
  }

  @Test
  @DisplayName("게시글을 작성하면 id로 조회할 수 있다")
  void createAndGet() {
    long id = postService.createPost("제목", "내용", Category.FREE, memberId);

    Post post = postService.getPost(id);

    assertEquals("제목", post.getTitle());
    assertEquals("내용", post.getContent());
    assertEquals(Category.FREE, post.getCategory());
    assertEquals("작성자", post.getAuthor().getName());
  }

  @Test
  @DisplayName("유효하지 않은 게시글은 저장되지 않는다")
  void createInvalid() {
    BaseException e =
        assertThrows(
            BaseException.class,
            () -> postService.createPost(" ", "내용", Category.FREE, memberId));

    assertEquals(PostErrorCode.TITLE_REQUIRED, e.getErrorCode());
    assertTrue(allPosts().isEmpty());
  }

  @Test
  @DisplayName("작성자 없이 게시글을 작성하면 예외가 발생한다")
  void createWithoutAuthor() {
    BaseException e =
        assertThrows(
            BaseException.class, () -> postService.createPost("제목", "내용", Category.FREE, null));

    assertEquals(PostErrorCode.AUTHOR_REQUIRED, e.getErrorCode());
  }

  @Test
  @DisplayName("존재하지 않는 회원으로 게시글을 작성하면 예외가 발생한다")
  void createWithUnknownAuthor() {
    BaseException e =
        assertThrows(
            BaseException.class,
            () -> postService.createPost("제목", "내용", Category.FREE, NOT_EXIST_ID));

    assertEquals(MemberErrorCode.MEMBER_NOT_FOUND, e.getErrorCode());
    assertTrue(allPosts().isEmpty());
  }

  @Test
  @DisplayName("게시글 목록에서 작성자 이름을 확인할 수 있다")
  void getPostsWithAuthorName() {
    postService.createPost("제목", "내용", Category.FREE, memberId);

    List<Post> posts = allPosts();

    assertEquals("작성자", posts.get(0).getAuthor().getName());
  }

  @Test
  @DisplayName("게시글 목록은 최신순으로 조회된다")
  void getPosts() {
    long first = postService.createPost("첫째", "내용", Category.FREE, memberId);
    long second = postService.createPost("둘째", "내용", Category.QUESTION, memberId);
    long third = postService.createPost("셋째", "내용", Category.INFO, memberId);

    List<Post> posts = allPosts();

    assertEquals(List.of("셋째", "둘째", "첫째"), posts.stream().map(Post::getTitle).toList());
    assertEquals(List.of(third, second, first), posts.stream().map(Post::getId).toList());
  }

  @Test
  @DisplayName("게시글 목록을 페이지 단위로 나눠 조회한다")
  void getPostsByPage() {
    List<Long> ids = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      ids.add(postService.createPost("제목" + i, "내용", Category.FREE, memberId));
    }

    Page<Post> first = postService.getPosts(0, 2);
    Page<Post> last = postService.getPosts(2, 2);

    assertEquals(List.of(ids.get(4), ids.get(3)), first.map(Post::getId).getContent());
    assertEquals(5, first.getTotalElements());
    assertEquals(3, first.getTotalPages());
    assertTrue(first.hasNext());
    assertEquals(List.of(ids.get(0)), last.map(Post::getId).getContent());
    assertFalse(last.hasNext());
  }

  @Test
  @DisplayName("삭제된 게시글은 목록과 전체 개수에서 제외된다")
  void getPostsExcludesDeleted() {
    long first = postService.createPost("첫째", "내용", Category.FREE, memberId);
    long second = postService.createPost("둘째", "내용", Category.FREE, memberId);

    postService.deletePost(first);

    Page<Post> page = postService.getPosts(0, 10);
    assertEquals(List.of(second), page.map(Post::getId).getContent());
    assertEquals(1, page.getTotalElements());
  }

  @ParameterizedTest
  @CsvSource({"-1, 10", "0, 0", "0, 101"})
  @DisplayName("페이지 번호나 크기가 범위를 벗어나면 예외가 발생한다")
  void getPostsWithInvalidPage(int page, int size) {
    BaseException e = assertThrows(BaseException.class, () -> postService.getPosts(page, size));

    assertEquals(PostErrorCode.INVALID_PAGE_REQUEST, e.getErrorCode());
  }

  @Test
  @DisplayName("존재하지 않는 게시글을 조회하면 예외가 발생한다")
  void getNotFound() {
    BaseException e = assertThrows(BaseException.class, () -> postService.getPost(NOT_EXIST_ID));

    assertEquals(PostErrorCode.POST_NOT_FOUND, e.getErrorCode());
  }

  @Test
  @DisplayName("게시글을 수정하면 다시 조회했을 때 수정된 값이 나온다")
  void update() {
    long id = postService.createPost("제목", "내용", Category.FREE, memberId);

    postService.updatePost(id, "새 제목", "새 내용", Category.NOTICE);

    Post post = postService.getPost(id);
    assertEquals("새 제목", post.getTitle());
    assertEquals("새 내용", post.getContent());
    assertEquals(Category.NOTICE, post.getCategory());
  }

  @Test
  @DisplayName("존재하지 않는 게시글을 수정하면 예외가 발생한다")
  void updateNotFound() {
    BaseException e =
        assertThrows(
            BaseException.class,
            () -> postService.updatePost(NOT_EXIST_ID, "새 제목", "새 내용", Category.NOTICE));

    assertEquals(PostErrorCode.POST_NOT_FOUND, e.getErrorCode());
  }

  @Test
  @DisplayName("게시글을 삭제하면 더 이상 조회되지 않는다")
  void delete() {
    long id = postService.createPost("제목", "내용", Category.FREE, memberId);

    postService.deletePost(id);

    BaseException e = assertThrows(BaseException.class, () -> postService.getPost(id));
    assertEquals(PostErrorCode.POST_NOT_FOUND, e.getErrorCode());
    assertTrue(allPosts().isEmpty());
  }

  @Test
  @DisplayName("게시글을 삭제해도 데이터는 남고 삭제 시각이 기록된다")
  void deleteIsSoft() {
    long id = postService.createPost("제목", "내용", Category.FREE, memberId);

    postService.deletePost(id);

    Integer deleted =
        jdbcTemplate.queryForObject(
            "select count(*) from post where id = ? and deleted_at is not null", Integer.class, id);
    assertEquals(1, deleted);
  }

  @Test
  @DisplayName("이미 삭제된 게시글은 수정하거나 다시 삭제할 수 없다")
  void updateOrDeleteDeleted() {
    long id = postService.createPost("제목", "내용", Category.FREE, memberId);
    postService.deletePost(id);

    BaseException update =
        assertThrows(
            BaseException.class,
            () -> postService.updatePost(id, "새 제목", "새 내용", Category.NOTICE));
    BaseException delete = assertThrows(BaseException.class, () -> postService.deletePost(id));

    assertEquals(PostErrorCode.POST_NOT_FOUND, update.getErrorCode());
    assertEquals(PostErrorCode.POST_NOT_FOUND, delete.getErrorCode());
  }

  @Test
  @DisplayName("존재하지 않는 게시글을 삭제하면 예외가 발생한다")
  void deleteNotFound() {
    BaseException e =
        assertThrows(BaseException.class, () -> postService.deletePost(NOT_EXIST_ID));

    assertEquals(PostErrorCode.POST_NOT_FOUND, e.getErrorCode());
  }

  @Test
  @DisplayName("앞선 게시글을 삭제해도 나머지 게시글의 id는 바뀌지 않는다")
  void idIsStableAfterDelete() {
    long first = postService.createPost("첫째", "내용", Category.FREE, memberId);
    long second = postService.createPost("둘째", "내용", Category.FREE, memberId);

    postService.deletePost(first);
    long third = postService.createPost("셋째", "내용", Category.FREE, memberId);

    assertEquals("둘째", postService.getPost(second).getTitle());
    assertEquals("셋째", postService.getPost(third).getTitle());
  }

  @Test
  @DisplayName("게시글을 상세 조회할 때마다 조회수가 1씩 증가한다")
  void getIncreasesViewCount() {
    long id = postService.createPost("제목", "내용", Category.FREE, memberId);

    assertEquals(1L, postService.getPost(id).getViewCount());
    assertEquals(2L, postService.getPost(id).getViewCount());
  }

  @Test
  @DisplayName("목록 조회, 수정, 삭제는 조회수를 증가시키지 않는다")
  void onlyGetIncreasesViewCount() {
    long first = postService.createPost("첫째", "내용", Category.FREE, memberId);
    long second = postService.createPost("둘째", "내용", Category.FREE, memberId);

    allPosts();
    postService.updatePost(first, "새 제목", "새 내용", Category.NOTICE);
    postService.deletePost(second);

    assertEquals(0L, allPosts().get(0).getViewCount());
  }

  @Test
  @DisplayName("게시글을 작성하면 작성일과 수정일이 기록된다")
  void createRecordsDates() {
    long id = postService.createPost("제목", "내용", Category.FREE, memberId);

    Post post = postService.getPost(id);

    assertNotNull(post.getCreatedAt());
    assertEquals(post.getCreatedAt(), post.getUpdatedAt());
  }

  @Test
  @DisplayName("게시글을 수정하면 수정일만 바뀌고 작성일은 유지된다")
  void updateChangesUpdatedAt() throws InterruptedException {
    long id = postService.createPost("제목", "내용", Category.FREE, memberId);
    Post before = postService.getPost(id);

    Thread.sleep(10);
    postService.updatePost(id, "새 제목", "새 내용", Category.NOTICE);

    Post after = postService.getPost(id);
    assertEquals(before.getCreatedAt(), after.getCreatedAt());
    assertTrue(after.getUpdatedAt().isAfter(before.getUpdatedAt()));
  }

  @Test
  @DisplayName("상세 조회로 조회수가 늘어나도 수정일은 바뀌지 않는다")
  void getDoesNotChangeUpdatedAt() throws InterruptedException {
    long id = postService.createPost("제목", "내용", Category.FREE, memberId);
    Post before = postService.getPost(id);

    Thread.sleep(10);
    Post after = postService.getPost(id);

    assertEquals(2L, after.getViewCount());
    assertEquals(before.getUpdatedAt(), after.getUpdatedAt());
  }

  private List<Post> allPosts() {
    return postService.getPosts(0, 100).getContent();
  }

  @Test
  @DisplayName("최대 길이의 제목과 내용도 DB에 그대로 저장된다")
  void createWithMaxLength() {
    String title = "가".repeat(Post.MAX_TITLE_LENGTH);
    String content = "가".repeat(Post.MAX_CONTENT_LENGTH);

    long id = postService.createPost(title, content, Category.FREE, memberId);

    Post post = postService.getPost(id);
    assertEquals(title, post.getTitle());
    assertEquals(content, post.getContent());
  }
}
