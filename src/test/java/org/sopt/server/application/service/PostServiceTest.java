package org.sopt.server.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.server.application.port.out.PostRepository;
import org.sopt.server.domain.Category;
import org.sopt.server.domain.Post;
import org.sopt.server.domain.exception.BaseException;
import org.sopt.server.domain.exception.PostErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PostServiceTest {
  private static final long NOT_EXIST_ID = Long.MAX_VALUE;

  @Autowired private PostService postService;
  @Autowired private PostRepository postRepository;

  @AfterEach
  void tearDown() {
    postRepository.deleteAll();
  }

  @Test
  @DisplayName("게시글을 작성하면 id로 조회할 수 있다")
  void createAndGet() {
    long id = postService.createPost("제목", "내용", Category.FREE, "작성자");

    Post post = postService.getPost(id);

    assertEquals("제목", post.getTitle());
    assertEquals("내용", post.getContent());
    assertEquals(Category.FREE, post.getCategory());
    assertEquals("작성자", post.getAuthor());
  }

  @Test
  @DisplayName("유효하지 않은 게시글은 저장되지 않는다")
  void createInvalid() {
    BaseException e =
        assertThrows(
            BaseException.class,
            () -> postService.createPost(" ", "내용", Category.FREE, "작성자"));

    assertEquals(PostErrorCode.TITLE_REQUIRED, e.getErrorCode());
    assertTrue(postService.getPosts().isEmpty());
  }

  @Test
  @DisplayName("게시글 목록은 id 순서로 조회된다")
  void getPosts() {
    long first = postService.createPost("첫째", "내용", Category.FREE, "작성자");
    long second = postService.createPost("둘째", "내용", Category.QUESTION, "작성자");
    long third = postService.createPost("셋째", "내용", Category.INFO, "작성자");

    List<Post> posts = postService.getPosts();

    assertEquals(List.of("첫째", "둘째", "셋째"), posts.stream().map(Post::getTitle).toList());
    assertEquals(List.of(first, second, third), posts.stream().map(Post::getId).toList());
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
    long id = postService.createPost("제목", "내용", Category.FREE, "작성자");

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
    long id = postService.createPost("제목", "내용", Category.FREE, "작성자");

    postService.deletePost(id);

    BaseException e = assertThrows(BaseException.class, () -> postService.getPost(id));
    assertEquals(PostErrorCode.POST_NOT_FOUND, e.getErrorCode());
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
    long first = postService.createPost("첫째", "내용", Category.FREE, "작성자");
    long second = postService.createPost("둘째", "내용", Category.FREE, "작성자");

    postService.deletePost(first);
    long third = postService.createPost("셋째", "내용", Category.FREE, "작성자");

    assertEquals("둘째", postService.getPost(second).getTitle());
    assertEquals("셋째", postService.getPost(third).getTitle());
  }

  @Test
  @DisplayName("게시글을 상세 조회할 때마다 조회수가 1씩 증가한다")
  void getIncreasesViewCount() {
    long id = postService.createPost("제목", "내용", Category.FREE, "작성자");

    assertEquals(1L, postService.getPost(id).getViewCount());
    assertEquals(2L, postService.getPost(id).getViewCount());
  }

  @Test
  @DisplayName("목록 조회, 수정, 삭제는 조회수를 증가시키지 않는다")
  void onlyGetIncreasesViewCount() {
    long first = postService.createPost("첫째", "내용", Category.FREE, "작성자");
    long second = postService.createPost("둘째", "내용", Category.FREE, "작성자");

    postService.getPosts();
    postService.updatePost(first, "새 제목", "새 내용", Category.NOTICE);
    postService.deletePost(second);

    assertEquals(0L, postService.getPosts().get(0).getViewCount());
  }
}
