package org.sopt.server.adapter.in;

import java.util.List;
import org.sopt.server.application.port.in.PostUseCase;
import org.sopt.server.domain.Category;

public class PostController {
  private final PostUseCase postUseCase;

  public PostController(PostUseCase postUseCase) {
    this.postUseCase = postUseCase;
  }

  public void createPost(String title, String content, String category, String author) {
    postUseCase.createPost(title, content, Category.from(category), author);
  }

  public List<PostResponse> getPosts() {
    return postUseCase.getPosts().stream().map(PostResponse::from).toList();
  }

  public PostResponse getPost(int number) {
    return PostResponse.from(postUseCase.getPost(number));
  }

  public void updatePost(int number, String title, String content, String category) {
    postUseCase.updatePost(number, title, content, Category.from(category));
  }

  public void deletePost(int number) {
    postUseCase.deletePost(number);
  }
}
