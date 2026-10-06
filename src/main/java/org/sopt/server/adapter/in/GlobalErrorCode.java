package org.sopt.server.adapter.in;

import org.sopt.server.domain.exception.ErrorCode;

public enum GlobalErrorCode implements ErrorCode {
  INTERNAL_SERVER_ERROR("GLOBAL-E001", "서버 내부 오류가 발생했습니다.");

  private final String code;
  private final String message;

  GlobalErrorCode(String code, String message) {
    this.code = code;
    this.message = message;
  }

  @Override
  public String getCode() {
    return this.code;
  }

  @Override
  public String getMessage() {
    return this.message;
  }
}
