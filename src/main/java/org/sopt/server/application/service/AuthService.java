package org.sopt.server.application.service;

import java.time.Instant;
import org.sopt.server.application.port.in.AuthTokens;
import org.sopt.server.application.port.in.AuthUseCase;
import org.sopt.server.application.port.out.MemberRepository;
import org.sopt.server.application.port.out.RefreshTokenRepository;
import org.sopt.server.application.port.out.TokenProvider;
import org.sopt.server.application.port.out.TokenProvider.IssuedRefreshToken;
import org.sopt.server.application.port.out.TokenProvider.RefreshTokenClaims;
import org.sopt.server.domain.Member;
import org.sopt.server.domain.RefreshToken;
import org.sopt.server.domain.exception.AuthErrorCode;
import org.sopt.server.domain.exception.BaseException;
import org.sopt.server.domain.exception.MemberErrorCode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService implements AuthUseCase {
  private final MemberRepository memberRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenProvider tokenProvider;

  public AuthService(
      MemberRepository memberRepository,
      RefreshTokenRepository refreshTokenRepository,
      PasswordEncoder passwordEncoder,
      TokenProvider tokenProvider) {
    this.memberRepository = memberRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.passwordEncoder = passwordEncoder;
    this.tokenProvider = tokenProvider;
  }

  @Override
  public Member signup(String name, String email, String password) {
    Member.validateRawPassword(password);
    if (memberRepository.existsByEmail(email)) {
      throw new BaseException(MemberErrorCode.DUPLICATE_EMAIL);
    }

    Member member = new Member(name, email, passwordEncoder.encode(password));
    try {
      return memberRepository.saveAndFlush(member);
    } catch (DataIntegrityViolationException e) {
      throw new BaseException(MemberErrorCode.DUPLICATE_EMAIL);
    }
  }

  @Override
  public AuthTokens login(String email, String password) {
    Member member =
        memberRepository
            .findByEmail(email)
            .orElseThrow(() -> new BaseException(AuthErrorCode.LOGIN_FAILED));

    if (password == null || !passwordEncoder.matches(password, member.getPassword())) {
      throw new BaseException(AuthErrorCode.LOGIN_FAILED);
    }

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
    if (!memberRepository.existsById(claims.memberId())) {
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

  private AuthTokens issueTokens(long memberId) {
    IssuedRefreshToken refreshToken = tokenProvider.createRefreshToken(memberId);
    refreshTokenRepository.save(
        new RefreshToken(memberId, refreshToken.jti(), refreshToken.expiresAt()));
    return new AuthTokens(tokenProvider.createAccessToken(memberId), refreshToken.value());
  }
}
