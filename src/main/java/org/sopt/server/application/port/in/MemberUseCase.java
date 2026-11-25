package org.sopt.server.application.port.in;

import org.sopt.server.domain.Member;

public interface MemberUseCase {
  Member getMember(long id);

  void deleteMember(long id);
}
