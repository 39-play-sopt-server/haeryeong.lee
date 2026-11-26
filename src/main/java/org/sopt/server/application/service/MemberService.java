package org.sopt.server.application.service;

import org.sopt.server.application.port.in.AuthUseCase;
import org.sopt.server.application.port.in.MemberUseCase;
import org.sopt.server.application.port.out.MemberRepository;
import org.sopt.server.application.port.out.PostRepository;
import org.sopt.server.domain.Member;
import org.sopt.server.domain.exception.BaseException;
import org.sopt.server.domain.exception.MemberErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MemberService implements MemberUseCase {
  private final MemberRepository memberRepository;
  private final PostRepository postRepository;
  private final AuthUseCase authUseCase;
  private final PasswordEncoder passwordEncoder;

  public MemberService(
      MemberRepository memberRepository,
      PostRepository postRepository,
      AuthUseCase authUseCase,
      PasswordEncoder passwordEncoder) {
    this.memberRepository = memberRepository;
    this.postRepository = postRepository;
    this.authUseCase = authUseCase;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional(readOnly = true)
  public Member getMember(long id) {
    return findMember(id);
  }

  @Override
  public void withdraw(long id, String password) {
    Member member = findMember(id);
    if (password == null || !passwordEncoder.matches(password, member.getPassword())) {
      throw new BaseException(MemberErrorCode.PASSWORD_MISMATCH);
    }
    member.withdraw();
    postRepository.deleteAllByAuthor(member, member.getDeletedAt());
    authUseCase.revokeAllRefreshTokens(id);
  }

  private Member findMember(long id) {
    return memberRepository
        .findById(id)
        .orElseThrow(() -> new BaseException(MemberErrorCode.MEMBER_NOT_FOUND));
  }
}
