package org.sopt.server.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sopt.server.domain.exception.BaseException;
import org.sopt.server.domain.exception.MemberErrorCode;

class MemberTest {
  private static final String NAME = "이해령";
  private static final String EMAIL = "sopt@sopt.org";
  private static final String PASSWORD = "encoded-password";

  @Test
  @DisplayName("유효한 이름, 이메일, 비밀번호로 회원을 생성한다")
  void create() {
    Member member = new Member(NAME, EMAIL, PASSWORD);

    assertNull(member.getId());
    assertEquals(NAME, member.getName());
    assertEquals(EMAIL, member.getEmail());
    assertEquals(PASSWORD, member.getPassword());
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
  @DisplayName("이름이 비어있거나 공백이면 생성할 수 없다")
  void createWithBlankName(String name) {
    BaseException e = assertThrows(BaseException.class, () -> new Member(name, EMAIL, PASSWORD));

    assertEquals(MemberErrorCode.NAME_REQUIRED, e.getErrorCode());
  }

  @Test
  @DisplayName("이름은 최대 길이까지 작성할 수 있다")
  void createWithMaxLength() {
    String name = "가".repeat(Member.MAX_NAME_LENGTH);

    assertEquals(name, new Member(name, EMAIL, PASSWORD).getName());
  }

  @Test
  @DisplayName("이름이 최대 길이를 넘으면 생성할 수 없다")
  void createWithTooLongName() {
    String name = "가".repeat(Member.MAX_NAME_LENGTH + 1);

    BaseException e = assertThrows(BaseException.class, () -> new Member(name, EMAIL, PASSWORD));

    assertEquals(MemberErrorCode.NAME_TOO_LONG, e.getErrorCode());
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", " "})
  @DisplayName("이메일이 비어있으면 생성할 수 없다")
  void createWithBlankEmail(String email) {
    BaseException e = assertThrows(BaseException.class, () -> new Member(NAME, email, PASSWORD));

    assertEquals(MemberErrorCode.EMAIL_REQUIRED, e.getErrorCode());
  }

  @ParameterizedTest
  @ValueSource(strings = {"sopt", "sopt@", "@sopt.org", "sopt@sopt", "so pt@sopt.org"})
  @DisplayName("이메일 형식이 올바르지 않으면 생성할 수 없다")
  void createWithInvalidEmail(String email) {
    BaseException e = assertThrows(BaseException.class, () -> new Member(NAME, email, PASSWORD));

    assertEquals(MemberErrorCode.EMAIL_INVALID, e.getErrorCode());
  }

  @Test
  @DisplayName("이메일이 최대 길이를 넘으면 생성할 수 없다")
  void createWithTooLongEmail() {
    String email = "a".repeat(Member.MAX_EMAIL_LENGTH - "@sopt.org".length() + 1) + "@sopt.org";

    BaseException e = assertThrows(BaseException.class, () -> new Member(NAME, email, PASSWORD));

    assertEquals(MemberErrorCode.EMAIL_INVALID, e.getErrorCode());
  }

  @ParameterizedTest
  @ValueSource(ints = {Member.MIN_PASSWORD_LENGTH, Member.MAX_PASSWORD_LENGTH})
  @DisplayName("평문 비밀번호는 최소~최대 길이 사이면 통과한다")
  void validRawPassword(int length) {
    assertDoesNotThrow(() -> Member.validateRawPassword("a".repeat(length)));
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "1234567", "123456789012345678901"})
  @DisplayName("평문 비밀번호가 없거나 길이 범위를 벗어나면 예외가 발생한다")
  void invalidRawPassword(String rawPassword) {
    BaseException e =
        assertThrows(BaseException.class, () -> Member.validateRawPassword(rawPassword));

    assertEquals(MemberErrorCode.PASSWORD_INVALID, e.getErrorCode());
  }

  @Test
  @DisplayName("탈퇴하면 개인정보를 익명화하고 삭제 시각을 기록한다")
  void withdraw() {
    Member member = new Member(NAME, EMAIL, PASSWORD);

    member.withdraw();

    assertEquals("탈퇴한 회원", member.getName());
    assertTrue(member.getEmail().startsWith("deleted_"));
    assertEquals("", member.getPassword());
    assertTrue(member.isDeleted());
  }
}
