package org.sopt.server.domain;

import java.time.LocalDateTime;
import org.sopt.server.domain.exception.BaseException;
import org.sopt.server.domain.exception.PostErrorCode;

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
      throw new BaseException(PostErrorCode.TITLE_REQUIRED);
    }
    if (content == null || content.isEmpty()) {
      throw new BaseException(PostErrorCode.CONTENT_REQUIRED);
    }
    if (category == null) {
      throw new BaseException(PostErrorCode.CATEGORY_REQUIRED);
    }
  }

  private void validateAuthor(String author) {
    if (author == null || author.isEmpty()) {
      throw new BaseException(PostErrorCode.AUTHOR_REQUIRED);
    }
  }
}
