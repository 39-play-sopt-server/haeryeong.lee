package org.sopt.server.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.server.domain.exception.BaseException;
import org.sopt.server.domain.exception.PostErrorCode;

@Getter
@RequiredArgsConstructor
public enum Category {
  FREE("자유"),
  QUESTION("질문"),
  INFO("정보"),
  NOTICE("공지");

  private final String label;

  public static Category from(String label) {
    for (Category category : values()) {
      if (category.label.equals(label)) {
        return category;
      }
    }
    throw new BaseException(PostErrorCode.CATEGORY_NOT_FOUND);
  }
}
