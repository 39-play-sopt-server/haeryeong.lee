package org.sopt.server.application.port.in;

import java.util.List;
import org.sopt.server.domain.Category;
import org.sopt.server.domain.Post;

public interface PostUseCase {
  void createPost(String title, String content, Category category, String author);

  List<Post> getPosts();

  Post getPost(long id);

  void updatePost(long id, String title, String content, Category category);

  void deletePost(long id);
}
