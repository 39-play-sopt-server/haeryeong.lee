package org.sopt.server.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.SQLRestriction;
import org.sopt.server.domain.exception.BaseException;
import org.sopt.server.domain.exception.MemberErrorCode;

@Entity
@SQLRestriction("deleted_at is null")
public class Member extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  protected Member() {}

  public Member(String name) {
    validateName(name);
    this.name = name;
  }

  public Long getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  private void validateName(String name) {
    if (name == null || name.isBlank()) {
      throw new BaseException(MemberErrorCode.NAME_REQUIRED);
    }
  }
}
