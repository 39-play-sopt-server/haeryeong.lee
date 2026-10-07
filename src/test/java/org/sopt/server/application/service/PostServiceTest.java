package org.sopt.server.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.server.adapter.out.InMemoryPostRepository;
import org.sopt.server.adapter.out.SequenceIdGenerator;
import org.sopt.server.domain.Category;
import org.sopt.server.domain.Post;
import org.sopt.server.domain.exception.BaseException;
import org.sopt.server.domain.exception.PostErrorCode;

class PostServiceTest {
  private PostService postService;

  @BeforeEach
  void setUp() {
    postService = new PostService(new InMemoryPostRepository(), new SequenceIdGenerator());
  }

  @Test
  @DisplayName("게시글을 작성하면 id로 조회할 수 있다")
  void createAndGet() {
    postService.createPost("제목", "내용", Category.FREE, "작성자");

    Post post = postService.getPost(1L);

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
    postService.createPost("첫째", "내용", Category.FREE, "작성자");
    postService.createPost("둘째", "내용", Category.QUESTION, "작성자");
    postService.createPost("셋째", "내용", Category.INFO, "작성자");

    List<Post> posts = postService.getPosts();

    assertEquals(List.of("첫째", "둘째", "셋째"), posts.stream().map(Post::getTitle).toList());
    assertEquals(List.of(1L, 2L, 3L), posts.stream().map(Post::getId).toList());
  }

  @Test
  @DisplayName("존재하지 않는 게시글을 조회하면 예외가 발생한다")
  void getNotFound() {
    BaseException e = assertThrows(BaseException.class, () -> postService.getPost(1L));

    assertEquals(PostErrorCode.POST_NOT_FOUND, e.getErrorCode());
  }

  @Test
  @DisplayName("게시글을 수정하면 다시 조회했을 때 수정된 값이 나온다")
  void update() {
    postService.createPost("제목", "내용", Category.FREE, "작성자");

    postService.updatePost(1L, "새 제목", "새 내용", Category.NOTICE);

    Post post = postService.getPost(1L);
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
            () -> postService.updatePost(1L, "새 제목", "새 내용", Category.NOTICE));

    assertEquals(PostErrorCode.POST_NOT_FOUND, e.getErrorCode());
  }

  @Test
  @DisplayName("게시글을 삭제하면 더 이상 조회되지 않는다")
  void delete() {
    postService.createPost("제목", "내용", Category.FREE, "작성자");

    postService.deletePost(1L);

    BaseException e = assertThrows(BaseException.class, () -> postService.getPost(1L));
    assertEquals(PostErrorCode.POST_NOT_FOUND, e.getErrorCode());
  }

  @Test
  @DisplayName("존재하지 않는 게시글을 삭제하면 예외가 발생한다")
  void deleteNotFound() {
    BaseException e = assertThrows(BaseException.class, () -> postService.deletePost(1L));

    assertEquals(PostErrorCode.POST_NOT_FOUND, e.getErrorCode());
  }

  @Test
  @DisplayName("앞선 게시글을 삭제해도 나머지 게시글의 id는 바뀌지 않는다")
  void idIsStableAfterDelete() {
    postService.createPost("첫째", "내용", Category.FREE, "작성자");
    postService.createPost("둘째", "내용", Category.FREE, "작성자");

    postService.deletePost(1L);
    postService.createPost("셋째", "내용", Category.FREE, "작성자");

    assertEquals("둘째", postService.getPost(2L).getTitle());
    assertEquals("셋째", postService.getPost(3L).getTitle());
  }
}
