package org.sopt.server.domain.exception;

public class BaseException extends RuntimeException {
  private final ErrorCode errorCode;

  public BaseException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public ErrorCode getErrorCode() {
    return this.errorCode;
  }
}
