package org.sopt.server.domain.exception;

public interface ErrorCode {
  ErrorType getType();

  String getCode();

  String getMessage();
}
