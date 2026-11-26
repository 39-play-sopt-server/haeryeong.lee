package org.sopt.server.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.server.security.jwt.JwtCodec.IssuedToken;
import org.sopt.server.security.jwt.JwtProperties.RefreshCookie;

class JwtCodecTest {
  private static final String SECRET = secret('a');
  private static final String ISSUER = "sopt-assignment";
  private static final Instant NOW = Instant.parse("2026-11-26T00:00:00Z");
  private static final Duration ACCESS_EXPIRATION = Duration.ofMinutes(30);
  private static final Duration REFRESH_EXPIRATION = Duration.ofDays(14);

  private final JwtCodec codec = codec(SECRET, ISSUER, NOW);

  @Test
  @DisplayName("발급한 토큰을 같은 종류로 검증하면 회원 ID와 jti를 꺼낸다")
  void issueAndVerify() {
    IssuedToken token = codec.issue(TokenType.REFRESH, 7L);

    JwtCodec.VerifiedToken verified = codec.verify(TokenType.REFRESH, token.value());

    assertEquals(7L, verified.memberId());
    assertEquals(token.jti(), verified.jti());
  }

  @Test
  @DisplayName("토큰 종류별 설정된 만료 시간으로 만료 시각을 계산한다")
  void expirationByType() {
    assertEquals(NOW.plus(ACCESS_EXPIRATION), codec.issue(TokenType.ACCESS, 1L).expiresAt());
    assertEquals(NOW.plus(REFRESH_EXPIRATION), codec.issue(TokenType.REFRESH, 1L).expiresAt());
  }

  @Test
  @DisplayName("발급할 때마다 서로 다른 jti를 부여한다")
  void uniqueJti() {
    assertNotEquals(codec.issue(TokenType.ACCESS, 1L).jti(), codec.issue(TokenType.ACCESS, 1L).jti());
  }

  @Test
  @DisplayName("다른 종류의 토큰으로 검증하면 실패한다")
  void typeMismatch() {
    String access = codec.issue(TokenType.ACCESS, 1L).value();
    String refresh = codec.issue(TokenType.REFRESH, 1L).value();

    assertThrows(InvalidTokenException.class, () -> codec.verify(TokenType.REFRESH, access));
    assertThrows(InvalidTokenException.class, () -> codec.verify(TokenType.ACCESS, refresh));
  }

  @Test
  @DisplayName("만료 시각이 지난 토큰은 검증에 실패한다")
  void expired() {
    String token = codec.issue(TokenType.ACCESS, 1L).value();
    JwtCodec later = codec(SECRET, ISSUER, NOW.plus(ACCESS_EXPIRATION).plusSeconds(1));

    assertThrows(InvalidTokenException.class, () -> later.verify(TokenType.ACCESS, token));
  }

  @Test
  @DisplayName("다른 키로 서명했거나 발급자가 다른 토큰은 검증에 실패한다")
  void signatureAndIssuerMismatch() {
    String otherKey = codec(secret('b'), ISSUER, NOW).issue(TokenType.ACCESS, 1L).value();
    String otherIssuer = codec(SECRET, "other-service", NOW).issue(TokenType.ACCESS, 1L).value();

    assertThrows(InvalidTokenException.class, () -> codec.verify(TokenType.ACCESS, otherKey));
    assertThrows(InvalidTokenException.class, () -> codec.verify(TokenType.ACCESS, otherIssuer));
  }

  @Test
  @DisplayName("형식이 잘못된 토큰은 라이브러리 예외 대신 InvalidTokenException으로 실패한다")
  void malformed() {
    assertThrows(InvalidTokenException.class, () -> codec.verify(TokenType.ACCESS, "invalid"));
    assertThrows(InvalidTokenException.class, () -> codec.verify(TokenType.ACCESS, ""));
    assertThrows(InvalidTokenException.class, () -> codec.verify(TokenType.ACCESS, null));
  }

  private static JwtCodec codec(String secret, String issuer, Instant now) {
    JwtProperties properties =
        new JwtProperties(
            secret,
            issuer,
            ACCESS_EXPIRATION,
            REFRESH_EXPIRATION,
            new RefreshCookie(true, "Strict"));
    return new JwtCodec(properties, Clock.fixed(now, ZoneOffset.UTC));
  }

  private static String secret(char seed) {
    return Base64.getEncoder().encodeToString(String.valueOf(seed).repeat(32).getBytes());
  }
}
