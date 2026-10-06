package org.sopt.server.adapter.in;

import org.sopt.server.domain.Post;

public record PostResponse(String title, String content) {
  public static PostResponse from(Post post) {
    return new PostResponse(post.getTitle(), post.getContent());
  }
}
