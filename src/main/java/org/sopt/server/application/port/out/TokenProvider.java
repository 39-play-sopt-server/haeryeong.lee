package org.sopt.server.application.port.out;

import java.time.Instant;

public interface TokenProvider {
  String createAccessToken(long memberId);

  IssuedRefreshToken createRefreshToken(long memberId);

  RefreshTokenClaims parseRefreshToken(String refreshToken);

  record IssuedRefreshToken(String value, String jti, Instant expiresAt) {}

  record RefreshTokenClaims(long memberId, String jti) {}
}
