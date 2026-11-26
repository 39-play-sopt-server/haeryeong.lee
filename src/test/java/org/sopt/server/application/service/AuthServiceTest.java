package org.sopt.server.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.server.application.port.in.AuthTokens;
import org.sopt.server.application.port.out.MemberRepository;
import org.sopt.server.application.port.out.RefreshTokenRepository;
import org.sopt.server.domain.Member;
import org.sopt.server.domain.exception.AuthErrorCode;
import org.sopt.server.domain.exception.BaseException;
import org.sopt.server.domain.exception.ErrorCode;
import org.sopt.server.domain.exception.MemberErrorCode;
import org.sopt.server.security.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class AuthServiceTest {
  private static final String NAME = "이해령";
  private static final String EMAIL = "sopt@sopt.org";
  private static final String PASSWORD = "password1234";

  @Autowired private AuthService authService;
  @Autowired private MemberService memberService;
  @Autowired private MemberRepository memberRepository;
  @Autowired private RefreshTokenRepository refreshTokenRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private JwtProvider jwtProvider;
  @Autowired private JdbcTemplate jdbcTemplate;

  @AfterEach
  void tearDown() {
    jdbcTemplate.update("delete from refresh_token");
    jdbcTemplate.update("delete from post");
    jdbcTemplate.update("delete from member");
  }

  @Test
  @DisplayName("회원가입하면 비밀번호가 해싱되어 저장된다")
  void signup() {
    Member member = authService.signup(NAME, EMAIL, PASSWORD);

    Member saved = memberRepository.findById(member.getId()).orElseThrow();
    assertEquals(EMAIL, saved.getEmail());
    assertNotEquals(PASSWORD, saved.getPassword());
    assertTrue(passwordEncoder.matches(PASSWORD, saved.getPassword()));
  }

  @Test
  @DisplayName("이미 가입된 이메일로는 가입할 수 없다")
  void signupDuplicateEmail() {
    authService.signup(NAME, EMAIL, PASSWORD);

    assertErrorCode(
        MemberErrorCode.DUPLICATE_EMAIL, () -> authService.signup("다른 회원", EMAIL, PASSWORD));
    assertEquals(1, memberRepository.count());
  }

  @Test
  @DisplayName("탈퇴한 회원의 이메일로 가입하면 500이 아닌 중복 이메일 예외가 발생한다")
  void signupWithDeletedMemberEmail() {
    long id = authService.signup(NAME, EMAIL, PASSWORD).getId();
    memberService.deleteMember(id);

    assertErrorCode(
        MemberErrorCode.DUPLICATE_EMAIL, () -> authService.signup(NAME, EMAIL, PASSWORD));
  }

  @Test
  @DisplayName("비밀번호 규칙을 지키지 않으면 가입할 수 없다")
  void signupInvalidPassword() {
    assertErrorCode(
        MemberErrorCode.PASSWORD_INVALID, () -> authService.signup(NAME, EMAIL, "short"));
    assertEquals(0, memberRepository.count());
  }

  @Test
  @DisplayName("이름이 비어있으면 가입할 수 없다")
  void signupBlankName() {
    assertErrorCode(MemberErrorCode.NAME_REQUIRED, () -> authService.signup(" ", EMAIL, PASSWORD));
    assertEquals(0, memberRepository.count());
  }

  @Test
  @DisplayName("로그인하면 회원 ID가 담긴 Access Token을 발급한다")
  void login() {
    long id = authService.signup(NAME, EMAIL, PASSWORD).getId();

    AuthTokens tokens = authService.login(EMAIL, PASSWORD);

    assertEquals(id, jwtProvider.getMemberId(tokens.accessToken()));
    assertEquals(id, jwtProvider.parseRefreshToken(tokens.refreshToken()).memberId());
    assertEquals(1, refreshTokenRepository.count());
  }

  @Test
  @DisplayName("Refresh Token으로 재발급하면 새 토큰 쌍을 발급하고 사용한 Refresh Token은 폐기한다")
  void reissue() {
    long id = authService.signup(NAME, EMAIL, PASSWORD).getId();
    AuthTokens tokens = authService.login(EMAIL, PASSWORD);

    AuthTokens reissued = authService.reissue(tokens.refreshToken());

    assertEquals(id, jwtProvider.getMemberId(reissued.accessToken()));
    assertNotEquals(tokens.refreshToken(), reissued.refreshToken());
    assertFalse(
        refreshTokenRepository.existsByJti(
            jwtProvider.parseRefreshToken(tokens.refreshToken()).jti()));
    assertTrue(
        refreshTokenRepository.existsByJti(
            jwtProvider.parseRefreshToken(reissued.refreshToken()).jti()));
  }

  @Test
  @DisplayName("이미 사용한 Refresh Token을 재사용하면 해당 회원의 모든 Refresh Token을 폐기한다")
  void reissueWithReusedToken() {
    authService.signup(NAME, EMAIL, PASSWORD);
    AuthTokens otherDevice = authService.login(EMAIL, PASSWORD);
    AuthTokens tokens = authService.login(EMAIL, PASSWORD);
    AuthTokens reissued = authService.reissue(tokens.refreshToken());

    assertErrorCode(
        AuthErrorCode.INVALID_REFRESH_TOKEN, () -> authService.reissue(tokens.refreshToken()));

    assertEquals(0, refreshTokenRepository.count());
    assertErrorCode(
        AuthErrorCode.INVALID_REFRESH_TOKEN, () -> authService.reissue(reissued.refreshToken()));
    assertErrorCode(
        AuthErrorCode.INVALID_REFRESH_TOKEN,
        () -> authService.reissue(otherDevice.refreshToken()));
  }

  @Test
  @DisplayName("Access Token이나 형식이 잘못된 토큰으로는 재발급할 수 없다")
  void reissueWithInvalidToken() {
    authService.signup(NAME, EMAIL, PASSWORD);
    AuthTokens tokens = authService.login(EMAIL, PASSWORD);

    assertErrorCode(
        AuthErrorCode.INVALID_REFRESH_TOKEN, () -> authService.reissue(tokens.accessToken()));
    assertErrorCode(AuthErrorCode.INVALID_REFRESH_TOKEN, () -> authService.reissue("invalid"));
    assertErrorCode(AuthErrorCode.REFRESH_TOKEN_REQUIRED, () -> authService.reissue(null));
  }

  @Test
  @DisplayName("탈퇴한 회원의 Refresh Token으로는 재발급할 수 없다")
  void reissueByDeletedMember() {
    long id = authService.signup(NAME, EMAIL, PASSWORD).getId();
    AuthTokens tokens = authService.login(EMAIL, PASSWORD);
    memberService.deleteMember(id);

    assertErrorCode(
        AuthErrorCode.INVALID_REFRESH_TOKEN, () -> authService.reissue(tokens.refreshToken()));
  }

  @Test
  @DisplayName("로그아웃하면 Refresh Token이 폐기되어 재발급할 수 없다")
  void logout() {
    authService.signup(NAME, EMAIL, PASSWORD);
    AuthTokens tokens = authService.login(EMAIL, PASSWORD);

    authService.logout(tokens.refreshToken());

    assertEquals(0, refreshTokenRepository.count());
    assertErrorCode(
        AuthErrorCode.INVALID_REFRESH_TOKEN, () -> authService.reissue(tokens.refreshToken()));
  }

  @Test
  @DisplayName("이메일이 없거나 비밀번호가 틀리면 같은 예외가 발생한다")
  void loginFailed() {
    authService.signup(NAME, EMAIL, PASSWORD);

    assertErrorCode(AuthErrorCode.LOGIN_FAILED, () -> authService.login("none@sopt.org", PASSWORD));
    assertErrorCode(AuthErrorCode.LOGIN_FAILED, () -> authService.login(EMAIL, "wrong-password"));
    assertErrorCode(AuthErrorCode.LOGIN_FAILED, () -> authService.login(EMAIL, null));
  }

  @Test
  @DisplayName("탈퇴한 회원은 로그인할 수 없다")
  void loginDeletedMember() {
    long id = authService.signup(NAME, EMAIL, PASSWORD).getId();
    memberService.deleteMember(id);

    assertErrorCode(AuthErrorCode.LOGIN_FAILED, () -> authService.login(EMAIL, PASSWORD));
  }

  private void assertErrorCode(ErrorCode expected, Runnable action) {
    BaseException e = assertThrows(BaseException.class, action::run);
    assertEquals(expected, e.getErrorCode());
  }
}
