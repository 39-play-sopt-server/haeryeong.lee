package org.sopt.server.security.jwt;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.sopt.server.application.port.out.TokenProvider;
import org.sopt.server.domain.exception.AuthErrorCode;
import org.sopt.server.domain.exception.BaseException;
import org.sopt.server.security.jwt.JwtCodec.IssuedToken;
import org.sopt.server.security.jwt.JwtCodec.VerifiedToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtProvider implements TokenProvider, AccessTokenResolver {
  private final JwtCodec jwtCodec;

  @Override
  public String createAccessToken(long memberId) {
    return jwtCodec.issue(TokenType.ACCESS, memberId).value();
  }

  @Override
  public IssuedRefreshToken createRefreshToken(long memberId) {
    IssuedToken token = jwtCodec.issue(TokenType.REFRESH, memberId);
    return new IssuedRefreshToken(token.value(), token.jti(), token.expiresAt());
  }

  @Override
  public RefreshTokenClaims parseRefreshToken(String refreshToken) {
    try {
      VerifiedToken token = jwtCodec.verify(TokenType.REFRESH, refreshToken);
      return new RefreshTokenClaims(token.memberId(), token.jti());
    } catch (InvalidTokenException e) {
      throw new BaseException(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }
  }

  @Override
  public Optional<Long> resolveMemberId(String accessToken) {
    try {
      return Optional.of(jwtCodec.verify(TokenType.ACCESS, accessToken).memberId());
    } catch (InvalidTokenException e) {
      return Optional.empty();
    }
  }
}
