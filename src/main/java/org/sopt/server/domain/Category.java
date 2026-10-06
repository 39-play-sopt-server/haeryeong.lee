package org.sopt.server.domain;

public enum Category {
  FREE("자유"),
  QUESTION("질문"),
  INFO("정보"),
  NOTICE("공지");

  private final String label;

  Category(String label) {
    this.label = label;
  }

  public String getLabel() {
    return this.label;
  }

  public static Category from(String label) {
    for (Category category : values()) {
      if (category.label.equals(label)) {
        return category;
      }
    }
    throw new IllegalArgumentException("존재하지 않는 카테고리입니다.");
  }
}
