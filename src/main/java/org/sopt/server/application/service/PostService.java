package org.sopt.server.application.service;

import java.util.List;
import org.sopt.server.application.port.in.PostUseCase;
import org.sopt.server.application.port.out.PostRepository;
import org.sopt.server.domain.Category;
import org.sopt.server.domain.Post;
import org.sopt.server.domain.exception.BaseException;
import org.sopt.server.domain.exception.PostErrorCode;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PostService implements PostUseCase {
  private final PostRepository postRepository;

  public PostService(PostRepository postRepository) {
    this.postRepository = postRepository;
  }

  @Override
  public long createPost(String title, String content, Category category, String author) {
    return postRepository.save(new Post(title, content, category, author)).getId();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Post> getPosts() {
    return postRepository.findAll(Sort.by("id"));
  }

  @Override
  public Post getPost(long id) {
    Post post = findPostForUpdate(id);
    post.increaseViewCount();
    return post;
  }

  @Override
  public void updatePost(long id, String title, String content, Category category) {
    Post post = findPostForUpdate(id);
    post.update(title, content, category);
  }

  @Override
  public void deletePost(long id) {
    Post post = findPostForUpdate(id);
    postRepository.delete(post);
  }

  private Post findPostForUpdate(long id) {
    return postRepository
        .findByIdForUpdate(id)
        .orElseThrow(() -> new BaseException(PostErrorCode.POST_NOT_FOUND));
  }
}
