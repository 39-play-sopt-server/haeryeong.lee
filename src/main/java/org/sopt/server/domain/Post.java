package org.sopt.server.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import org.sopt.server.domain.exception.BaseException;
import org.sopt.server.domain.exception.PostErrorCode;

@Entity
public class Post {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title;
  private String content;

  @Enumerated(EnumType.STRING)
  private Category category;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member author;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private long viewCount;

  protected Post() {}

  public Post(String title, String content, Category category, Member author) {
    validate(title, content, category);
    validateAuthor(author);
    this.title = title;
    this.content = content;
    this.category = category;
    this.author = author;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = this.createdAt;
  }

  public Long getId() {
    return this.id;
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

  public Member getAuthor() {
    return this.author;
  }

  public LocalDateTime getCreatedAt() {
    return this.createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return this.updatedAt;
  }

  public long getViewCount() {
    return this.viewCount;
  }

  public void increaseViewCount() {
    this.viewCount++;
  }

  public void update(String title, String content, Category category) {
    validate(title, content, category);
    this.title = title;
    this.content = content;
    this.category = category;
    this.updatedAt = LocalDateTime.now();
  }

  private void validate(String title, String content, Category category) {
    if (title == null || title.isBlank()) {
      throw new BaseException(PostErrorCode.TITLE_REQUIRED);
    }
    if (content == null || content.isBlank()) {
      throw new BaseException(PostErrorCode.CONTENT_REQUIRED);
    }
    if (category == null) {
      throw new BaseException(PostErrorCode.CATEGORY_REQUIRED);
    }
  }

  private void validateAuthor(Member author) {
    if (author == null) {
      throw new BaseException(PostErrorCode.AUTHOR_REQUIRED);
    }
  }
}
