package org.sopt.server.application.port.out;

import java.util.List;
import java.util.Optional;
import org.sopt.server.domain.Post;

public interface PostRepository {
  void save(Post post);

  List<Post> findAll();

  Optional<Post> findByNumber(int number);

  void deleteByNumber(int number);
}
