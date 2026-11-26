package org.sopt.server.security.jwt;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TokenType {
  ACCESS("access"),
  REFRESH("refresh");

  private final String claimValue;

  public String claimValue() {
    return this.claimValue;
  }
}
