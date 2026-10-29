package org.sopt.server.application.port.in;

import org.sopt.server.domain.Category;
import org.sopt.server.domain.Post;
import org.springframework.data.domain.Page;

public interface PostUseCase {
  long createPost(String title, String content, Category category, Long memberId);

  Page<Post> getPosts(int page, int size);

  Post getPost(long id);

  void updatePost(long id, String title, String content, Category category);

  void deletePost(long id);
}
