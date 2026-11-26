package org.sopt.server.security.jwt;

public enum TokenType {
  ACCESS("access"),
  REFRESH("refresh");

  private final String claimValue;

  TokenType(String claimValue) {
    this.claimValue = claimValue;
  }

  public String claimValue() {
    return this.claimValue;
  }
}
