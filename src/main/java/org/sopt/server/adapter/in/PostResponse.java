package org.sopt.server.adapter.in;

import java.time.LocalDateTime;
import org.sopt.server.domain.Post;

public record PostResponse(
    String title,
    String content,
    String category,
    String author,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
  public static PostResponse from(Post post) {
    return new PostResponse(
        post.getTitle(),
        post.getContent(),
        post.getCategory().getLabel(),
        post.getAuthor(),
        post.getCreatedAt(),
        post.getUpdatedAt());
  }
}
