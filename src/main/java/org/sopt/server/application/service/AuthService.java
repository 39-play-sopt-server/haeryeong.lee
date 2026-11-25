package org.sopt.server.application.service;

import org.sopt.server.application.port.in.AuthUseCase;
import org.sopt.server.application.port.out.MemberRepository;
import org.sopt.server.application.port.out.TokenProvider;
import org.sopt.server.domain.Member;
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
  private final PasswordEncoder passwordEncoder;
  private final TokenProvider tokenProvider;

  public AuthService(
      MemberRepository memberRepository,
      PasswordEncoder passwordEncoder,
      TokenProvider tokenProvider) {
    this.memberRepository = memberRepository;
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
  @Transactional(readOnly = true)
  public String login(String email, String password) {
    Member member =
        memberRepository
            .findByEmail(email)
            .orElseThrow(() -> new BaseException(AuthErrorCode.LOGIN_FAILED));

    if (password == null || !passwordEncoder.matches(password, member.getPassword())) {
      throw new BaseException(AuthErrorCode.LOGIN_FAILED);
    }

    return tokenProvider.createAccessToken(member.getId());
  }
}
