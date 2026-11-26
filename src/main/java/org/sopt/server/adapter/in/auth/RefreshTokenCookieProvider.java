package org.sopt.server.adapter.in.auth;

import java.time.Duration;
import org.sopt.server.security.jwt.JwtProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookieProvider {
  public static final String COOKIE_NAME = "refresh_token";
  private static final String COOKIE_PATH = "/api/v1/auth";

  private final Duration maxAge;
  private final boolean secure;
  private final String sameSite;

  public RefreshTokenCookieProvider(JwtProperties properties) {
    this.maxAge = properties.refreshTokenExpiration();
    this.secure = properties.refreshCookie().secure();
    this.sameSite = properties.refreshCookie().sameSite();
  }

  public ResponseCookie create(String refreshToken) {
    return baseCookie(refreshToken).maxAge(maxAge).build();
  }

  public ResponseCookie expire() {
    return baseCookie("").maxAge(Duration.ZERO).build();
  }

  private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
    return ResponseCookie.from(COOKIE_NAME, value)
        .httpOnly(true)
        .secure(secure)
        .sameSite(sameSite)
        .path(COOKIE_PATH);
  }
}
