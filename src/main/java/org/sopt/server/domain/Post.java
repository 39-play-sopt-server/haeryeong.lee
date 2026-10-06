package org.sopt.server.domain;

import java.time.LocalDateTime;

public class Post {
  private String title;
  private String content;
  private Category category;
  private final String author;
  private final LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public Post(String title, String content, Category category, String author) {
    validate(title, content, category);
    validateAuthor(author);
    this.title = title;
    this.content = content;
    this.category = category;
    this.author = author;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = this.createdAt;
  }

  public String getTitle() {
    return this.title;
  }

  public String getContent() {
    return this.content;
  }

  public Category getCategory() {
    return this.category;
  }

  public String getAuthor() {
    return this.author;
  }

  public LocalDateTime getCreatedAt() {
    return this.createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return this.updatedAt;
  }

  public void update(String title, String content, Category category) {
    validate(title, content, category);
    this.title = title;
    this.content = content;
    this.category = category;
    this.updatedAt = LocalDateTime.now();
  }

  private void validate(String title, String content, Category category) {
    if (title == null || title.isEmpty()) {
      throw new IllegalArgumentException("제목을 작성해주세요.");
    }
    if (content == null || content.isEmpty()) {
      throw new IllegalArgumentException("내용을 작성해주세요.");
    }
    if (category == null) {
      throw new IllegalArgumentException("카테고리를 선택해주세요.");
    }
  }

  private void validateAuthor(String author) {
    if (author == null || author.isEmpty()) {
      throw new IllegalArgumentException("작성자를 작성해주세요.");
    }
  }
}
