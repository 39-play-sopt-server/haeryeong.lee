package org.sopt.server.application.port.in;

import java.util.Optional;
import org.sopt.server.domain.Member;

public interface MemberUseCase {
  Member register(String name, String email, String encodedPassword);

  Member getMember(long id);

  Optional<Member> findByEmail(String email);

  boolean isActiveMember(long id);

  void withdraw(long id, String password);
}
