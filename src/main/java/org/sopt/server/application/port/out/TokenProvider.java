package org.sopt.server.application.port.out;

public interface TokenProvider {
  String createAccessToken(long memberId);
}
