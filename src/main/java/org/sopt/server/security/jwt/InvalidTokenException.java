package org.sopt.server.security.jwt;

public class InvalidTokenException extends RuntimeException {
  public InvalidTokenException(Throwable cause) {
    super(cause);
  }
}
