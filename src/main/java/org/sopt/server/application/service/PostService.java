package org.sopt.server.application.service;

import java.util.List;
import java.util.Optional;
import org.sopt.server.application.port.in.PostUseCase;
import org.sopt.server.application.port.out.PostRepository;
import org.sopt.server.domain.Post;

public class PostService implements PostUseCase {
  private final PostRepository postRepository;

  public PostService(PostRepository postRepository) {
    this.postRepository = postRepository;
  }

  @Override
  public void createPost(String title, String content) {
    postRepository.save(new Post(title, content));
  }

  @Override
  public List<Post> getPosts() {
    return postRepository.findAll();
  }

  @Override
  public Optional<Post> getPost(int number) {
    return postRepository.findByNumber(number);
  }

  @Override
  public boolean updatePost(int number, String title, String content) {
    Optional<Post> post = postRepository.findByNumber(number);
    if (post.isEmpty()) {
      return false;
    }
    post.get().updateTitle(title);
    post.get().updateContent(content);
    return true;
  }

  @Override
  public boolean deletePost(int number) {
    return postRepository.deleteByNumber(number);
  }
}
