package org.sopt.server.security.jwt;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("jwt")
public record JwtProperties(
    String secret,
    String issuer,
    Duration accessTokenExpiration,
    Duration refreshTokenExpiration,
    RefreshCookie refreshCookie) {

  public Duration expirationOf(TokenType type) {
    return switch (type) {
      case ACCESS -> accessTokenExpiration;
      case REFRESH -> refreshTokenExpiration;
    };
  }

  public record RefreshCookie(boolean secure, String sameSite) {}
}
