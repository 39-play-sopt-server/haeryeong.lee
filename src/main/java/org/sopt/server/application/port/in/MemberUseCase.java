package org.sopt.server.application.port.in;

import org.sopt.server.domain.Member;

public interface MemberUseCase {
  Member createMember(String name);

  void deleteMember(long id);
}
