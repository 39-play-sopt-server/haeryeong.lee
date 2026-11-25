package org.sopt.server.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.sopt.server.application.port.out.TokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider implements TokenProvider {
  private final SecretKey secretKey;
  private final long accessTokenExpiration;

  public JwtProvider(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.access-token-expiration}") long accessTokenExpiration) {
    this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    this.accessTokenExpiration = accessTokenExpiration;
  }

  @Override
  public String createAccessToken(long memberId) {
    Date now = new Date();

    return Jwts.builder()
        .subject(String.valueOf(memberId))
        .issuedAt(now)
        .expiration(new Date(now.getTime() + accessTokenExpiration))
        .signWith(secretKey)
        .compact();
  }

  public long getMemberId(String token) {
    Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();

    return Long.parseLong(claims.getSubject());
  }
}
