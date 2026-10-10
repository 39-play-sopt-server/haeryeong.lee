package org.sopt.server.domain.exception;

public enum PostErrorCode implements ErrorCode {
  POST_NOT_FOUND("POST-E001", "존재하지 않는 게시글입니다."),
  TITLE_REQUIRED("POST-E002", "제목을 작성해주세요."),
  CONTENT_REQUIRED("POST-E003", "내용을 작성해주세요."),
  CATEGORY_REQUIRED("POST-E004", "카테고리를 선택해주세요."),
  AUTHOR_REQUIRED("POST-E005", "작성자를 작성해주세요."),
  CATEGORY_NOT_FOUND("POST-E006", "존재하지 않는 카테고리입니다.");

  private final String code;
  private final String message;

  PostErrorCode(String code, String message) {
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
