package org.sopt.server.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sopt.server.domain.exception.BaseException;
import org.sopt.server.domain.exception.PostErrorCode;

class PostTest {

  @Test
  @DisplayName("유효한 값으로 게시글을 생성하면 수정일은 작성일과 같다")
  void create() {
    Post post = new Post(1L, "제목", "내용", Category.FREE, "작성자");

    assertEquals(1L, post.getId());
    assertEquals("제목", post.getTitle());
    assertEquals("내용", post.getContent());
    assertEquals(Category.FREE, post.getCategory());
    assertEquals("작성자", post.getAuthor());
    assertEquals(post.getCreatedAt(), post.getUpdatedAt());
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
  @DisplayName("제목이 비어있거나 공백이면 생성할 수 없다")
  void createWithBlankTitle(String title) {
    BaseException e =
        assertThrows(
            BaseException.class, () -> new Post(1L, title, "내용", Category.FREE, "작성자"));

    assertEquals(PostErrorCode.TITLE_REQUIRED, e.getErrorCode());
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
  @DisplayName("내용이 비어있거나 공백이면 생성할 수 없다")
  void createWithBlankContent(String content) {
    BaseException e =
        assertThrows(
            BaseException.class, () -> new Post(1L, "제목", content, Category.FREE, "작성자"));

    assertEquals(PostErrorCode.CONTENT_REQUIRED, e.getErrorCode());
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
  @DisplayName("작성자가 비어있거나 공백이면 생성할 수 없다")
  void createWithBlankAuthor(String author) {
    BaseException e =
        assertThrows(
            BaseException.class, () -> new Post(1L, "제목", "내용", Category.FREE, author));

    assertEquals(PostErrorCode.AUTHOR_REQUIRED, e.getErrorCode());
  }

  @Test
  @DisplayName("카테고리가 없으면 생성할 수 없다")
  void createWithoutCategory() {
    BaseException e =
        assertThrows(BaseException.class, () -> new Post(1L, "제목", "내용", null, "작성자"));

    assertEquals(PostErrorCode.CATEGORY_REQUIRED, e.getErrorCode());
  }

  @Test
  @DisplayName("수정하면 제목, 내용, 카테고리가 바뀌고 작성자와 작성일은 유지된다")
  void update() {
    Post post = new Post(1L, "제목", "내용", Category.FREE, "작성자");

    post.update("새 제목", "새 내용", Category.NOTICE);

    assertEquals("새 제목", post.getTitle());
    assertEquals("새 내용", post.getContent());
    assertEquals(Category.NOTICE, post.getCategory());
    assertEquals("작성자", post.getAuthor());
    assertFalse(post.getUpdatedAt().isBefore(post.getCreatedAt()));
  }

  @Test
  @DisplayName("수정 값이 유효하지 않으면 기존 값이 그대로 유지된다")
  void updateWithBlankContent() {
    Post post = new Post(1L, "제목", "내용", Category.FREE, "작성자");

    BaseException e =
        assertThrows(BaseException.class, () -> post.update("새 제목", " ", Category.NOTICE));

    assertEquals(PostErrorCode.CONTENT_REQUIRED, e.getErrorCode());
    assertEquals("제목", post.getTitle());
    assertEquals("내용", post.getContent());
    assertEquals(Category.FREE, post.getCategory());
  }
}
