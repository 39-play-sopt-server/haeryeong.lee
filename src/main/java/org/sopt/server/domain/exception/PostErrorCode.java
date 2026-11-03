package org.sopt.server.domain.exception;

public enum PostErrorCode implements ErrorCode {
  POST_NOT_FOUND(ErrorType.NOT_FOUND, "POST-E001", "존재하지 않는 게시글입니다."),
  TITLE_REQUIRED(ErrorType.INVALID_INPUT, "POST-E002", "제목을 작성해주세요."),
  CONTENT_REQUIRED(ErrorType.INVALID_INPUT, "POST-E003", "내용을 작성해주세요."),
  CATEGORY_REQUIRED(ErrorType.INVALID_INPUT, "POST-E004", "카테고리를 선택해주세요."),
  AUTHOR_REQUIRED(ErrorType.INVALID_INPUT, "POST-E005", "작성자를 작성해주세요."),
  CATEGORY_NOT_FOUND(ErrorType.INVALID_INPUT, "POST-E006", "존재하지 않는 카테고리입니다."),
  INVALID_PAGE_REQUEST(
      ErrorType.INVALID_INPUT, "POST-E007", "페이지는 0 이상, 크기는 1 이상 100 이하로 요청해주세요."),
  TITLE_TOO_LONG(ErrorType.INVALID_INPUT, "POST-E008", "제목은 100자 이하로 작성해주세요."),
  CONTENT_TOO_LONG(ErrorType.INVALID_INPUT, "POST-E009", "내용은 10,000자 이하로 작성해주세요.");

  private final ErrorType type;
  private final String code;
  private final String message;

  PostErrorCode(ErrorType type, String code, String message) {
    this.type = type;
    this.code = code;
    this.message = message;
  }

  @Override
  public ErrorType getType() {
    return this.type;
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
