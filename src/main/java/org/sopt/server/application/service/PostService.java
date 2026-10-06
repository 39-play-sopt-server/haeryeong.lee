package org.sopt.server.application.service;

import java.util.List;
import org.sopt.server.application.port.in.PostUseCase;
import org.sopt.server.application.port.out.PostRepository;
import org.sopt.server.domain.Category;
import org.sopt.server.domain.Post;
import org.sopt.server.domain.exception.BaseException;
import org.sopt.server.domain.exception.PostErrorCode;

public class PostService implements PostUseCase {
  private final PostRepository postRepository;

  public PostService(PostRepository postRepository) {
    this.postRepository = postRepository;
  }

  @Override
  public void createPost(String title, String content, Category category, String author) {
    postRepository.save(new Post(title, content, category, author));
  }

  @Override
  public List<Post> getPosts() {
    return postRepository.findAll();
  }

  @Override
  public Post getPost(int number) {
    return postRepository
        .findByNumber(number)
        .orElseThrow(() -> new BaseException(PostErrorCode.POST_NOT_FOUND));
  }

  @Override
  public void updatePost(int number, String title, String content, Category category) {
    getPost(number).update(title, content, category);
  }

  @Override
  public void deletePost(int number) {
    getPost(number);
    postRepository.deleteByNumber(number);
  }
}
