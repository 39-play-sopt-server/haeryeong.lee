package org.sopt.server.domain.exception;

public enum MemberErrorCode implements ErrorCode {
  MEMBER_NOT_FOUND(ErrorType.NOT_FOUND, "MEMBER-E001", "존재하지 않는 회원입니다."),
  NAME_REQUIRED(ErrorType.INVALID_INPUT, "MEMBER-E002", "이름을 작성해주세요."),
  NAME_TOO_LONG(ErrorType.INVALID_INPUT, "MEMBER-E003", "이름은 30자 이하로 작성해주세요."),
  EMAIL_REQUIRED(ErrorType.INVALID_INPUT, "MEMBER-E004", "이메일을 작성해주세요."),
  EMAIL_INVALID(ErrorType.INVALID_INPUT, "MEMBER-E005", "올바른 형식의 이메일을 100자 이하로 작성해주세요."),
  PASSWORD_INVALID(ErrorType.INVALID_INPUT, "MEMBER-E006", "비밀번호는 8자 이상 20자 이하로 작성해주세요."),
  DUPLICATE_EMAIL(ErrorType.CONFLICT, "MEMBER-E007", "이미 가입된 이메일입니다."),
  PASSWORD_MISMATCH(ErrorType.INVALID_INPUT, "MEMBER-E008", "비밀번호가 올바르지 않습니다.");

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
