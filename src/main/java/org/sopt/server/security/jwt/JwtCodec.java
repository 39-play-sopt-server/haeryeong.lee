package org.sopt.server.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtCodec {
  private static final String TOKEN_TYPE_CLAIM = "type";

  private final SecretKey secretKey;
  private final JwtProperties properties;
  private final Clock clock;

  public JwtCodec(JwtProperties properties, Clock clock) {
    this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
    this.properties = properties;
    this.clock = clock;
  }

  public IssuedToken issue(TokenType type, long memberId) {
    Instant issuedAt = clock.instant();
    Instant expiresAt = issuedAt.plus(properties.expirationOf(type));
    String jti = UUID.randomUUID().toString();

    String value =
        Jwts.builder()
            .issuer(properties.issuer())
            .subject(String.valueOf(memberId))
            .id(jti)
            .claim(TOKEN_TYPE_CLAIM, type.claimValue())
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(expiresAt))
            .signWith(secretKey)
            .compact();
    return new IssuedToken(value, jti, expiresAt);
  }

  public VerifiedToken verify(TokenType type, String token) {
    try {
      Claims claims =
          Jwts.parser()
              .verifyWith(secretKey)
              .clock(() -> Date.from(clock.instant()))
              .requireIssuer(properties.issuer())
              .require(TOKEN_TYPE_CLAIM, type.claimValue())
              .build()
              .parseSignedClaims(token)
              .getPayload();
      if (claims.getId() == null) {
        throw new InvalidTokenException(null);
      }
      return new VerifiedToken(Long.parseLong(claims.getSubject()), claims.getId());
    } catch (JwtException | IllegalArgumentException e) {
      throw new InvalidTokenException(e);
    }
  }

  public record IssuedToken(String value, String jti, Instant expiresAt) {}

  public record VerifiedToken(long memberId, String jti) {}
}
