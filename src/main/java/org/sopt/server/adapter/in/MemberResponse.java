package org.sopt.server.adapter.in;

import org.sopt.server.domain.Member;

public record MemberResponse(long id, String name) {
  public static MemberResponse from(Member member) {
    return new MemberResponse(member.getId(), member.getName());
  }
}
