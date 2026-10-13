package org.sopt.server.adapter.in;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.sopt.server.domain.exception.BaseException;
import org.sopt.server.domain.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = Logger.getLogger(GlobalExceptionHandler.class.getName());

  @ExceptionHandler(BaseException.class)
  public ResponseEntity<BaseResponse<Void>> handleBaseException(BaseException e) {
    ErrorCode errorCode = e.getErrorCode();
    HttpStatus status =
        switch (errorCode.getType()) {
          case INVALID_INPUT -> HttpStatus.BAD_REQUEST;
          case NOT_FOUND -> HttpStatus.NOT_FOUND;
          case INTERNAL -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    return failure(status, errorCode);
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class
  })
  public ResponseEntity<BaseResponse<Void>> handleInvalidRequest(Exception e) {
    return failure(HttpStatus.BAD_REQUEST, GlobalErrorCode.INVALID_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<BaseResponse<Void>> handleException(Exception e) {
    // 없는 경로, 지원하지 않는 메서드 등 Spring MVC가 던지는 4xx 예외는 상태 코드를 그대로 따른다
    if (e instanceof ErrorResponse errorResponse
        && errorResponse.getStatusCode().is4xxClientError()) {
      return failure(errorResponse.getStatusCode(), GlobalErrorCode.INVALID_REQUEST);
    }
    log.log(Level.SEVERE, "[UnexpectedException]", e);
    return failure(HttpStatus.INTERNAL_SERVER_ERROR, GlobalErrorCode.INTERNAL_SERVER_ERROR);
  }

  private ResponseEntity<BaseResponse<Void>> failure(HttpStatusCode status, ErrorCode errorCode) {
    return ResponseEntity.status(status).body(BaseResponse.failure(errorCode));
  }
}
