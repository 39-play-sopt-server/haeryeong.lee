package org.sopt.server.application.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.sopt.server.application.event.MemberWithdrawnEvent;
import org.sopt.server.application.port.in.MemberUseCase;
import org.sopt.server.application.port.out.MemberRepository;
import org.sopt.server.domain.Member;
import org.sopt.server.domain.exception.BaseException;
import org.sopt.server.domain.exception.MemberErrorCode;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements MemberUseCase {
  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  public Member register(String name, String email, String encodedPassword) {
    if (memberRepository.existsByEmail(email)) {
      throw new BaseException(MemberErrorCode.DUPLICATE_EMAIL);
    }

    Member member = new Member(name, email, encodedPassword);
    try {
      return memberRepository.saveAndFlush(member);
    } catch (DataIntegrityViolationException e) {
      throw new BaseException(MemberErrorCode.DUPLICATE_EMAIL);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Member getMember(long id) {
    return findMember(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Member> findByEmail(String email) {
    return memberRepository.findByEmail(email);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isActiveMember(long id) {
    return memberRepository.existsById(id);
  }

  @Override
  public void withdraw(long id, String password) {
    Member member = findMember(id);
    if (password == null || !passwordEncoder.matches(password, member.getPassword())) {
      throw new BaseException(MemberErrorCode.PASSWORD_MISMATCH);
    }
    member.withdraw();
    eventPublisher.publishEvent(new MemberWithdrawnEvent(id, member.getDeletedAt()));
  }

  private Member findMember(long id) {
    return memberRepository
        .findById(id)
        .orElseThrow(() -> new BaseException(MemberErrorCode.MEMBER_NOT_FOUND));
  }
}
