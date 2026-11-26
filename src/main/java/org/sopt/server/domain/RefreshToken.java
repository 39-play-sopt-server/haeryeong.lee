package org.sopt.server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(indexes = @Index(name = "idx_refresh_token_member_id", columnList = "member_id"))
public class RefreshToken {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long memberId;

  @Column(nullable = false, unique = true, length = 36)
  private String jti;

  @Column(nullable = false)
  private Instant expiresAt;

  protected RefreshToken() {}

  public RefreshToken(Long memberId, String jti, Instant expiresAt) {
    this.memberId = memberId;
    this.jti = jti;
    this.expiresAt = expiresAt;
  }

  public Long getId() {
    return this.id;
  }

  public Long getMemberId() {
    return this.memberId;
  }

  public String getJti() {
    return this.jti;
  }

  public Instant getExpiresAt() {
    return this.expiresAt;
  }
}
