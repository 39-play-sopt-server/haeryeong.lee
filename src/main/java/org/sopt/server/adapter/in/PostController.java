package org.sopt.server.adapter.in;

import java.util.List;
import java.util.Optional;
import org.sopt.server.application.port.in.PostUseCase;

public class PostController {
  private final PostUseCase postUseCase;

  public PostController(PostUseCase postUseCase) {
    this.postUseCase = postUseCase;
  }

  public void createPost(String title, String content) {
    postUseCase.createPost(title, content);
  }

  public List<PostResponse> getPosts() {
    return postUseCase.getPosts().stream().map(PostResponse::from).toList();
  }

  public Optional<PostResponse> getPost(int number) {
    return postUseCase.getPost(number).map(PostResponse::from);
  }

  public boolean updatePost(int number, String title, String content) {
    return postUseCase.updatePost(number, title, content);
  }

  public boolean deletePost(int number) {
    return postUseCase.deletePost(number);
  }
}
