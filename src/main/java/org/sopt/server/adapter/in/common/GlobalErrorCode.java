package org.sopt.server.adapter.in.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.server.domain.exception.ErrorCode;
import org.sopt.server.domain.exception.ErrorType;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {
  INTERNAL_SERVER_ERROR(ErrorType.INTERNAL, "GLOBAL-E001", "서버 내부 오류가 발생했습니다."),
  INVALID_REQUEST(ErrorType.INVALID_INPUT, "GLOBAL-E002", "잘못된 요청입니다.");

  private final ErrorType type;
  private final String code;
  private final String message;
}
