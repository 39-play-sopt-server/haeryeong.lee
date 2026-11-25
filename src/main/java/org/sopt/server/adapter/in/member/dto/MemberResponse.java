package org.sopt.server.adapter.in.member.dto;

import org.sopt.server.domain.Member;

public record MemberResponse(long id, String name, String email) {
  public static MemberResponse from(Member member) {
    return new MemberResponse(member.getId(), member.getName(), member.getEmail());
  }
}
