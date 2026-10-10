package org.sopt.server.application.service;

import java.util.List;
import org.sopt.server.application.port.in.PostUseCase;
import org.sopt.server.application.port.out.IdGenerator;
import org.sopt.server.application.port.out.PostRepository;
import org.sopt.server.domain.Category;
import org.sopt.server.domain.Post;
import org.sopt.server.domain.exception.BaseException;
import org.sopt.server.domain.exception.PostErrorCode;

public class PostService implements PostUseCase {
  private final PostRepository postRepository;
  private final IdGenerator idGenerator;

  public PostService(PostRepository postRepository, IdGenerator idGenerator) {
    this.postRepository = postRepository;
    this.idGenerator = idGenerator;
  }

  @Override
  public void createPost(String title, String content, Category category, String author) {
    postRepository.save(new Post(idGenerator.nextId(), title, content, category, author));
  }

  @Override
  public List<Post> getPosts() {
    return postRepository.findAll();
  }

  @Override
  public Post getPost(long id) {
    return postRepository
        .findById(id)
        .orElseThrow(() -> new BaseException(PostErrorCode.POST_NOT_FOUND));
  }

  @Override
  public void updatePost(long id, String title, String content, Category category) {
    Post post = getPost(id);
    post.update(title, content, category);
    postRepository.save(post);
  }

  @Override
  public void deletePost(long id) {
    getPost(id);
    postRepository.deleteById(id);
  }
}
