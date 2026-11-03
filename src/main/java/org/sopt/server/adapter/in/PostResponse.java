package org.sopt.server.adapter.in;

import java.time.LocalDateTime;
import org.sopt.server.domain.Post;

public record PostResponse(
    long id,
    String title,
    String content,
    String category,
    String author,
    long viewCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
  public static PostResponse from(Post post) {
    return new PostResponse(
        post.getId(),
        post.getTitle(),
        post.getContent(),
        post.getCategory().getLabel(),
        post.getAuthor().getName(),
        post.getViewCount(),
        post.getCreatedAt(),
        post.getUpdatedAt());
  }
}
