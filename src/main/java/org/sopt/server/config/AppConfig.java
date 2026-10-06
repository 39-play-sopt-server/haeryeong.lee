package org.sopt.server.config;

import org.sopt.server.adapter.in.PostController;
import org.sopt.server.adapter.out.InMemoryPostRepository;
import org.sopt.server.application.port.in.PostUseCase;
import org.sopt.server.application.port.out.PostRepository;
import org.sopt.server.application.service.PostService;

public class AppConfig {
  public static PostController postController() {
    PostRepository postRepository = new InMemoryPostRepository();
    PostUseCase postUseCase = new PostService(postRepository);
    return new PostController(postUseCase);
  }
}
