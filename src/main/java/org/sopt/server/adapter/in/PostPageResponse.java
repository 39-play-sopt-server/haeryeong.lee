package org.sopt.server.adapter.in;

import java.util.List;
import org.sopt.server.domain.Post;
import org.springframework.data.domain.Page;

public record PostPageResponse(
    List<PostResponse> posts,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext) {
  public static PostPageResponse from(Page<Post> page) {
    return new PostPageResponse(
        page.getContent().stream().map(PostResponse::from).toList(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.hasNext());
  }
}
