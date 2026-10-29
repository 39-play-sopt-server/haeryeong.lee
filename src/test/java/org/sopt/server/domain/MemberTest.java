package org.sopt.server.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sopt.server.domain.exception.BaseException;
import org.sopt.server.domain.exception.MemberErrorCode;

class MemberTest {

  @Test
  @DisplayName("유효한 이름으로 회원을 생성한다")
  void create() {
    Member member = new Member("이해령");

    assertNull(member.getId());
    assertEquals("이해령", member.getName());
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
  @DisplayName("이름이 비어있거나 공백이면 생성할 수 없다")
  void createWithBlankName(String name) {
    BaseException e = assertThrows(BaseException.class, () -> new Member(name));

    assertEquals(MemberErrorCode.NAME_REQUIRED, e.getErrorCode());
  }
}
