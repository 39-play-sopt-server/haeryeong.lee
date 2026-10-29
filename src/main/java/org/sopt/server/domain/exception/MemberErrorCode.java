package org.sopt.server.domain.exception;

public enum MemberErrorCode implements ErrorCode {
  MEMBER_NOT_FOUND(ErrorType.NOT_FOUND, "MEMBER-E001", "존재하지 않는 회원입니다."),
  NAME_REQUIRED(ErrorType.INVALID_INPUT, "MEMBER-E002", "이름을 작성해주세요.");

  private final ErrorType type;
  private final String code;
  private final String message;

  MemberErrorCode(ErrorType type, String code, String message) {
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
