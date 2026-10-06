package org.sopt.server.domain;

public class Post {
  private String title;
  private String content;

  public Post(String title, String content) {
    validate(title, content);
    this.title = title;
    this.content = content;
  }

  public String getTitle() {
    return this.title;
  }

  public String getContent() {
    return this.content;
  }

  public void update(String title, String content) {
    validate(title, content);
    this.title = title;
    this.content = content;
  }

  private void validate(String title, String content) {
    if (title == null || title.isEmpty()) {
      throw new IllegalArgumentException("제목을 작성해주세요.");
    }
    if (content == null || content.isEmpty()) {
      throw new IllegalArgumentException("내용을 작성해주세요.");
    }
  }
}