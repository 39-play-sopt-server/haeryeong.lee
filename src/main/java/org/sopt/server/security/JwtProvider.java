package org.sopt.server.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.sopt.server.application.port.out.TokenProvider;
import org.sopt.server.domain.exception.AuthErrorCode;
import org.sopt.server.domain.exception.BaseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider implements TokenProvider {
  private static final String TOKEN_TYPE_CLAIM = "type";
  private static final String ACCESS_TOKEN_TYPE = "access";
  private static final String REFRESH_TOKEN_TYPE = "refresh";

  private final SecretKey secretKey;
  private final String issuer;
  private final long accessTokenExpiration;
  private final long refreshTokenExpiration;

  public JwtProvider(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.issuer}") String issuer,
      @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
      @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
    this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    this.issuer = issuer;
    this.accessTokenExpiration = accessTokenExpiration;
    this.refreshTokenExpiration = refreshTokenExpiration;
  }

  @Override
  public String createAccessToken(long memberId) {
    Instant now = Instant.now();

    return Jwts.builder()
        .issuer(issuer)
        .subject(String.valueOf(memberId))
        .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusMillis(accessTokenExpiration)))
        .signWith(secretKey)
        .compact();
  }

  @Override
  public IssuedRefreshToken createRefreshToken(long memberId) {
    Instant now = Instant.now();
    Instant expiresAt = now.plusMillis(refreshTokenExpiration);
    String jti = UUID.randomUUID().toString();

    String token =
        Jwts.builder()
            .issuer(issuer)
            .subject(String.valueOf(memberId))
            .id(jti)
            .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(secretKey)
            .compact();
    return new IssuedRefreshToken(token, jti, expiresAt);
  }

  @Override
  public RefreshTokenClaims parseRefreshToken(String refreshToken) {
    try {
      Claims claims = parse(refreshToken, REFRESH_TOKEN_TYPE);
      if (claims.getId() == null) {
        throw new BaseException(AuthErrorCode.INVALID_REFRESH_TOKEN);
      }
      return new RefreshTokenClaims(Long.parseLong(claims.getSubject()), claims.getId());
    } catch (JwtException | IllegalArgumentException e) {
      throw new BaseException(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }
  }

  public long getMemberId(String accessToken) {
    return Long.parseLong(parse(accessToken, ACCESS_TOKEN_TYPE).getSubject());
  }

  private Claims parse(String token, String expectedType) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .requireIssuer(issuer)
        .require(TOKEN_TYPE_CLAIM, expectedType)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
