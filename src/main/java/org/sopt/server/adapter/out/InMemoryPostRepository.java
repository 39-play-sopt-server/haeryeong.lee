package org.sopt.server.adapter.out;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.sopt.server.application.port.out.PostRepository;
import org.sopt.server.domain.Post;

public class InMemoryPostRepository implements PostRepository {
  private final List<Post> posts = new ArrayList<>();

  @Override
  public void save(Post post) {
    posts.add(post);
  }

  @Override
  public List<Post> findAll() {
    return List.copyOf(posts);
  }

  @Override
  public Optional<Post> findByNumber(int number) {
    if (!isValidNumber(number)) {
      return Optional.empty();
    }
    return Optional.of(posts.get(number - 1));
  }

  @Override
  public void deleteByNumber(int number) {
    if (isValidNumber(number)) {
      posts.remove(number - 1);
    }
  }

  private boolean isValidNumber(int number) {
    return number >= 1 && number <= posts.size();
  }
}
