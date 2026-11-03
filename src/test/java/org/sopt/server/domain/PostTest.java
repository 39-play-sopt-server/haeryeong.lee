package org.sopt.server.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sopt.server.domain.exception.BaseException;
import org.sopt.server.domain.exception.PostErrorCode;

class PostTest {
  private static final Member AUTHOR = new Member("작성자");

  @Test
  @DisplayName("유효한 값으로 게시글을 생성한다")
  void create() {
    Post post = new Post("제목", "내용", Category.FREE, AUTHOR);

    assertNull(post.getId());
    assertEquals("제목", post.getTitle());
    assertEquals("내용", post.getContent());
    assertEquals(Category.FREE, post.getCategory());
    assertEquals(AUTHOR, post.getAuthor());
    assertFalse(post.isDeleted());
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
  @DisplayName("제목이 비어있거나 공백이면 생성할 수 없다")
  void createWithBlankTitle(String title) {
    BaseException e =
        assertThrows(
            BaseException.class, () -> new Post(title, "내용", Category.FREE, AUTHOR));

    assertEquals(PostErrorCode.TITLE_REQUIRED, e.getErrorCode());
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
  @DisplayName("내용이 비어있거나 공백이면 생성할 수 없다")
  void createWithBlankContent(String content) {
    BaseException e =
        assertThrows(
            BaseException.class, () -> new Post("제목", content, Category.FREE, AUTHOR));

    assertEquals(PostErrorCode.CONTENT_REQUIRED, e.getErrorCode());
  }

  @Test
  @DisplayName("작성자가 없으면 생성할 수 없다")
  void createWithoutAuthor() {
    BaseException e =
        assertThrows(BaseException.class, () -> new Post("제목", "내용", Category.FREE, null));

    assertEquals(PostErrorCode.AUTHOR_REQUIRED, e.getErrorCode());
  }

  @Test
  @DisplayName("카테고리가 없으면 생성할 수 없다")
  void createWithoutCategory() {
    BaseException e =
        assertThrows(BaseException.class, () -> new Post("제목", "내용", null, AUTHOR));

    assertEquals(PostErrorCode.CATEGORY_REQUIRED, e.getErrorCode());
  }

  @Test
  @DisplayName("수정하면 제목, 내용, 카테고리가 바뀌고 작성자는 유지된다")
  void update() {
    Post post = new Post("제목", "내용", Category.FREE, AUTHOR);

    post.update("새 제목", "새 내용", Category.NOTICE);

    assertEquals("새 제목", post.getTitle());
    assertEquals("새 내용", post.getContent());
    assertEquals(Category.NOTICE, post.getCategory());
    assertEquals(AUTHOR, post.getAuthor());
  }

  @Test
  @DisplayName("삭제하면 삭제 시각이 기록되고 삭제된 상태가 된다")
  void delete() {
    Post post = new Post("제목", "내용", Category.FREE, AUTHOR);

    post.delete();

    assertTrue(post.isDeleted());
    assertNotNull(post.getDeletedAt());
  }

  @Test
  @DisplayName("수정 값이 유효하지 않으면 기존 값이 그대로 유지된다")
  void updateWithBlankContent() {
    Post post = new Post("제목", "내용", Category.FREE, AUTHOR);

    BaseException e =
        assertThrows(BaseException.class, () -> post.update("새 제목", " ", Category.NOTICE));

    assertEquals(PostErrorCode.CONTENT_REQUIRED, e.getErrorCode());
    assertEquals("제목", post.getTitle());
    assertEquals("내용", post.getContent());
    assertEquals(Category.FREE, post.getCategory());
  }

  @Test
  @DisplayName("제목과 내용은 최대 길이까지 작성할 수 있다")
  void createWithMaxLength() {
    String title = "가".repeat(Post.MAX_TITLE_LENGTH);
    String content = "가".repeat(Post.MAX_CONTENT_LENGTH);

    Post post = new Post(title, content, Category.FREE, AUTHOR);

    assertEquals(title, post.getTitle());
    assertEquals(content, post.getContent());
  }

  @Test
  @DisplayName("제목이 최대 길이를 넘으면 생성할 수 없다")
  void createWithTooLongTitle() {
    String title = "가".repeat(Post.MAX_TITLE_LENGTH + 1);

    BaseException e =
        assertThrows(BaseException.class, () -> new Post(title, "내용", Category.FREE, AUTHOR));

    assertEquals(PostErrorCode.TITLE_TOO_LONG, e.getErrorCode());
  }

  @Test
  @DisplayName("내용이 최대 길이를 넘으면 생성할 수 없다")
  void createWithTooLongContent() {
    String content = "가".repeat(Post.MAX_CONTENT_LENGTH + 1);

    BaseException e =
        assertThrows(BaseException.class, () -> new Post("제목", content, Category.FREE, AUTHOR));

    assertEquals(PostErrorCode.CONTENT_TOO_LONG, e.getErrorCode());
  }

  @Test
  @DisplayName("수정할 때도 최대 길이를 넘을 수 없다")
  void updateWithTooLongTitle() {
    Post post = new Post("제목", "내용", Category.FREE, AUTHOR);
    String title = "가".repeat(Post.MAX_TITLE_LENGTH + 1);

    BaseException e =
        assertThrows(BaseException.class, () -> post.update(title, "내용", Category.FREE));

    assertEquals(PostErrorCode.TITLE_TOO_LONG, e.getErrorCode());
    assertEquals("제목", post.getTitle());
  }
}
