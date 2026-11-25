package org.sopt.server.application.service;

import org.sopt.server.application.port.in.MemberUseCase;
import org.sopt.server.application.port.out.MemberRepository;
import org.sopt.server.application.port.out.PostRepository;
import org.sopt.server.domain.Member;
import org.sopt.server.domain.exception.BaseException;
import org.sopt.server.domain.exception.MemberErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MemberService implements MemberUseCase {
  private final MemberRepository memberRepository;
  private final PostRepository postRepository;

  public MemberService(MemberRepository memberRepository, PostRepository postRepository) {
    this.memberRepository = memberRepository;
    this.postRepository = postRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public Member getMember(long id) {
    return findMember(id);
  }

  @Override
  public void deleteMember(long id) {
    Member member = findMember(id);
    member.delete();
    postRepository.deleteAllByAuthor(member, member.getDeletedAt());
  }

  private Member findMember(long id) {
    return memberRepository
        .findById(id)
        .orElseThrow(() -> new BaseException(MemberErrorCode.MEMBER_NOT_FOUND));
  }
}
