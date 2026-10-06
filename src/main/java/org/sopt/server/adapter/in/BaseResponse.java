package org.sopt.server.adapter.in;

import org.sopt.server.domain.exception.ErrorCode;

public record BaseResponse<T>(boolean success, String code, String message, T data) {
  private static final String SUCCESS_CODE = "SUCCESS";
  private static final String SUCCESS_MESSAGE = "요청에 성공했습니다.";

  public static <T> BaseResponse<T> success(T data) {
    return new BaseResponse<>(true, SUCCESS_CODE, SUCCESS_MESSAGE, data);
  }

  public static <T> BaseResponse<T> failure(ErrorCode errorCode) {
    return new BaseResponse<>(false, errorCode.getCode(), errorCode.getMessage(), null);
  }
}
