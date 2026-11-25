package org.sopt.server.application.port.in;

import org.sopt.server.domain.Member;

public interface AuthUseCase {
  Member signup(String name, String email, String password);

  String login(String email, String password);
}
