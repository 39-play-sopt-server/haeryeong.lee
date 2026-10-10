package org.sopt.server.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sopt.server.domain.exception.BaseException;
import org.sopt.server.domain.exception.PostErrorCode;

class CategoryTest {

  @ParameterizedTest
  @EnumSource(Category.class)
  @DisplayName("라벨로 카테고리를 찾는다")
  void from(Category category) {
    assertEquals(category, Category.from(category.getLabel()));
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "잡담", "FREE", " 자유"})
  @DisplayName("존재하지 않는 라벨이면 예외가 발생한다")
  void fromUnknownLabel(String label) {
    BaseException e = assertThrows(BaseException.class, () -> Category.from(label));

    assertEquals(PostErrorCode.CATEGORY_NOT_FOUND, e.getErrorCode());
  }

  @Test
  @DisplayName("자유 라벨은 FREE이다")
  void fromFree() {
    assertEquals(Category.FREE, Category.from("자유"));
  }
}
