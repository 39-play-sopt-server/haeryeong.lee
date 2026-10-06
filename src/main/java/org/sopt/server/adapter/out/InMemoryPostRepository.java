package org.sopt.server.adapter.out;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.sopt.server.application.port.out.PostRepository;
import org.sopt.server.domain.Post;

public class InMemoryPostRepository implements PostRepository {
  private final Map<Long, Post> posts = new HashMap<>();

  @Override
  public void save(Post post) {
    posts.put(post.getId(), post);
  }

  @Override
  public List<Post> findAll() {
    return posts.values().stream().sorted(Comparator.comparing(Post::getId)).toList();
  }

  @Override
  public Optional<Post> findById(long id) {
    return Optional.ofNullable(posts.get(id));
  }

  @Override
  public void deleteById(long id) {
    posts.remove(id);
  }
}
