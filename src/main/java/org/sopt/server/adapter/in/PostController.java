package org.sopt.server.adapter.in;

import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sopt.server.application.port.in.PostUseCase;
import org.sopt.server.domain.Category;
import org.sopt.server.domain.exception.BaseException;

public class PostController {
  private static final Logger log = Logger.getLogger(PostController.class.getName());

  private final PostUseCase postUseCase;

  public PostController(PostUseCase postUseCase) {
    this.postUseCase = postUseCase;
  }

  public BaseResponse<Void> createPost(PostCreateRequest request) {
    return handleVoid(
        () ->
            postUseCase.createPost(
                request.title(),
                request.content(),
                Category.from(request.category()),
                request.author()));
  }

  public BaseResponse<List<PostResponse>> getPosts() {
    return handle(() -> postUseCase.getPosts().stream().map(PostResponse::from).toList());
  }

  public BaseResponse<PostResponse> getPost(long id) {
    return handle(() -> PostResponse.from(postUseCase.getPost(id)));
  }

  public BaseResponse<Void> updatePost(long id, PostUpdateRequest request) {
    return handleVoid(
        () ->
            postUseCase.updatePost(
                id, request.title(), request.content(), Category.from(request.category())));
  }

  public BaseResponse<Void> deletePost(long id) {
    return handleVoid(() -> postUseCase.deletePost(id));
  }

  private <T> BaseResponse<T> handle(Supplier<T> action) {
    try {
      return BaseResponse.success(action.get());
    } catch (BaseException e) {
      return BaseResponse.failure(e.getErrorCode());
    } catch (Exception e) {
      log.log(Level.SEVERE, "[UnexpectedException]", e);
      return BaseResponse.failure(GlobalErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  private BaseResponse<Void> handleVoid(Runnable action) {
    return handle(
        () -> {
          action.run();
          return null;
        });
  }
}
