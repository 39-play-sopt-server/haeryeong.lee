package org.sopt.server.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
  UNAUTHORIZED(ErrorType.UNAUTHORIZED, "AUTH-E001", "인증이 필요합니다."),
  LOGIN_FAILED(ErrorType.UNAUTHORIZED, "AUTH-E002", "이메일 또는 비밀번호가 올바르지 않습니다."),
  REFRESH_TOKEN_REQUIRED(ErrorType.UNAUTHORIZED, "AUTH-E003", "Refresh Token이 필요합니다."),
  INVALID_REFRESH_TOKEN(
      ErrorType.UNAUTHORIZED, "AUTH-E004", "유효하지 않은 Refresh Token입니다. 다시 로그인해주세요.");

  private final ErrorType type;
  private final String code;
  private final String message;
}
