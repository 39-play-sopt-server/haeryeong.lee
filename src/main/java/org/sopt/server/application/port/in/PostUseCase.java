package org.sopt.server.application.port.in;

import java.util.List;
import java.util.Optional;
import org.sopt.server.domain.Category;
import org.sopt.server.domain.Post;

public interface PostUseCase {
  void createPost(String title, String content, Category category, String author);

  List<Post> getPosts();

  Optional<Post> getPost(int number);

  boolean updatePost(int number, String title, String content, Category category);

  boolean deletePost(int number);
}
