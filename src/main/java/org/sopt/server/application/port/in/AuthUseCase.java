package org.sopt.server.application.port.in;

import org.sopt.server.domain.Member;

public interface AuthUseCase {
  Member signup(String name, String email, String password);

  AuthTokens login(String email, String password);

  AuthTokens reissue(String refreshToken);

  void logout(String refreshToken);

  void revokeAllRefreshTokens(long memberId);

  int deleteExpiredRefreshTokens();
}
