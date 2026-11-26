package org.sopt.server.application.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.sopt.server.application.event.MemberWithdrawnEvent;
import org.sopt.server.application.port.in.AuthTokens;
import org.sopt.server.application.port.in.AuthUseCase;
import org.sopt.server.application.port.in.MemberUseCase;
import org.sopt.server.application.port.out.RefreshTokenRepository;
import org.sopt.server.application.port.out.TokenProvider;
import org.sopt.server.application.port.out.TokenProvider.IssuedRefreshToken;
import org.sopt.server.application.port.out.TokenProvider.RefreshTokenClaims;
import org.sopt.server.domain.Member;
import org.sopt.server.domain.RefreshToken;
import org.sopt.server.domain.exception.AuthErrorCode;
import org.sopt.server.domain.exception.BaseException;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService implements AuthUseCase {
  private final MemberUseCase memberUseCase;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenProvider tokenProvider;
  private final String dummyPasswordHash;

  public AuthService(
      MemberUseCase memberUseCase,
      RefreshTokenRepository refreshTokenRepository,
      PasswordEncoder passwordEncoder,
      TokenProvider tokenProvider) {
    this.memberUseCase = memberUseCase;
    this.refreshTokenRepository = refreshTokenRepository;
    this.passwordEncoder = passwordEncoder;
    this.tokenProvider = tokenProvider;
    this.dummyPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());
  }

  @Override
  public Member signup(String name, String email, String password) {
    Member.validateRawPassword(password);
    return memberUseCase.register(name, email, passwordEncoder.encode(password));
  }

  @Override
  public AuthTokens login(String email, String password) {
    Optional<Member> found = memberUseCase.findByEmail(email);
    String encodedPassword = found.map(Member::getPassword).orElse(dummyPasswordHash);
    boolean matched = passwordEncoder.matches(password == null ? "" : password, encodedPassword);

    if (found.isEmpty() || password == null || !matched) {
      throw new BaseException(AuthErrorCode.LOGIN_FAILED);
    }

    Member member = found.get();
    refreshTokenRepository.deleteExpiredByMemberId(member.getId(), Instant.now());
    return issueTokens(member.getId());
  }

  @Override
  @Transactional(noRollbackFor = BaseException.class)
  public AuthTokens reissue(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new BaseException(AuthErrorCode.REFRESH_TOKEN_REQUIRED);
    }

    RefreshTokenClaims claims = tokenProvider.parseRefreshToken(refreshToken);

    if (refreshTokenRepository.deleteByJti(claims.jti()) == 0) {
      refreshTokenRepository.deleteAllByMemberId(claims.memberId());
      throw new BaseException(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }
    if (!memberUseCase.isActiveMember(claims.memberId())) {
      throw new BaseException(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    return issueTokens(claims.memberId());
  }

  @Override
  public void logout(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      return;
    }
    RefreshTokenClaims claims;
    try {
      claims = tokenProvider.parseRefreshToken(refreshToken);
    } catch (BaseException e) {
      return;
    }
    refreshTokenRepository.deleteByJti(claims.jti());
  }

  @EventListener
  public void revokeAllRefreshTokens(MemberWithdrawnEvent event) {
    refreshTokenRepository.deleteAllByMemberId(event.memberId());
  }

  @Override
  public int deleteExpiredRefreshTokens() {
    return refreshTokenRepository.deleteAllExpired(Instant.now());
  }

  private AuthTokens issueTokens(long memberId) {
    IssuedRefreshToken refreshToken = tokenProvider.createRefreshToken(memberId);
    refreshTokenRepository.save(
        new RefreshToken(memberId, refreshToken.jti(), refreshToken.expiresAt()));
    return new AuthTokens(tokenProvider.createAccessToken(memberId), refreshToken.value());
  }
}
