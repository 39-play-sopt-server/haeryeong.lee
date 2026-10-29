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
  public Member createMember(String name) {
    return memberRepository.save(new Member(name));
  }

  @Override
  public void deleteMember(long id) {
    Member member =
        memberRepository
            .findById(id)
            .orElseThrow(() -> new BaseException(MemberErrorCode.MEMBER_NOT_FOUND));
    member.delete();
    postRepository.deleteAllByAuthor(member, member.getDeletedAt());
  }
}
