package org.sopt.server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.regex.Pattern;
import org.hibernate.annotations.SQLRestriction;
import org.sopt.server.domain.exception.BaseException;
import org.sopt.server.domain.exception.MemberErrorCode;

@Entity
@SQLRestriction("deleted_at is null")
public class Member extends BaseEntity {
  public static final int MAX_NAME_LENGTH = 30;
  public static final int MAX_EMAIL_LENGTH = 100;
  public static final int MIN_PASSWORD_LENGTH = 8;
  public static final int MAX_PASSWORD_LENGTH = 20;

  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = MAX_NAME_LENGTH)
  private String name;

  @Column(nullable = false, unique = true, length = MAX_EMAIL_LENGTH)
  private String email;

  @Column(nullable = false)
  private String password;

  protected Member() {}

  public Member(String name, String email, String password) {
    validateName(name);
    validateEmail(email);
    this.name = name;
    this.email = email;
    this.password = password;
  }

  public static void validateRawPassword(String rawPassword) {
    if (rawPassword == null
        || rawPassword.length() < MIN_PASSWORD_LENGTH
        || rawPassword.length() > MAX_PASSWORD_LENGTH) {
      throw new BaseException(MemberErrorCode.PASSWORD_INVALID);
    }
  }

  public Long getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public String getEmail() {
    return this.email;
  }

  public String getPassword() {
    return this.password;
  }

  private void validateName(String name) {
    if (name == null || name.isBlank()) {
      throw new BaseException(MemberErrorCode.NAME_REQUIRED);
    }
    if (name.length() > MAX_NAME_LENGTH) {
      throw new BaseException(MemberErrorCode.NAME_TOO_LONG);
    }
  }

  private void validateEmail(String email) {
    if (email == null || email.isBlank()) {
      throw new BaseException(MemberErrorCode.EMAIL_REQUIRED);
    }
    if (email.length() > MAX_EMAIL_LENGTH || !EMAIL_PATTERN.matcher(email).matches()) {
      throw new BaseException(MemberErrorCode.EMAIL_INVALID);
    }
  }
}
