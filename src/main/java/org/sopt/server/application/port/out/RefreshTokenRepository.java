package org.sopt.server.application.port.out;

import java.time.Instant;
import org.sopt.server.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  boolean existsByJti(String jti);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("delete from RefreshToken r where r.jti = :jti")
  int deleteByJti(@Param("jti") String jti);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("delete from RefreshToken r where r.memberId = :memberId")
  int deleteAllByMemberId(@Param("memberId") long memberId);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("delete from RefreshToken r where r.memberId = :memberId and r.expiresAt < :now")
  int deleteExpiredByMemberId(@Param("memberId") long memberId, @Param("now") Instant now);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("delete from RefreshToken r where r.expiresAt < :now")
  int deleteAllExpired(@Param("now") Instant now);
}
