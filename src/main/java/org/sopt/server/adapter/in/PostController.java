package org.sopt.server.adapter.in;

import java.util.List;
import java.util.function.Supplier;
import org.sopt.server.application.port.in.PostUseCase;
import org.sopt.server.domain.Category;
import org.sopt.server.domain.exception.BaseException;

public class PostController {
  private final PostUseCase postUseCase;

  public PostController(PostUseCase postUseCase) {
    this.postUseCase = postUseCase;
  }

  public BaseResponse<Void> createPost(
      String title, String content, String category, String author) {
    return handleVoid(
        () -> postUseCase.createPost(title, content, Category.from(category), author));
  }

  public BaseResponse<List<PostResponse>> getPosts() {
    return handle(() -> postUseCase.getPosts().stream().map(PostResponse::from).toList());
  }

  public BaseResponse<PostResponse> getPost(long id) {
    return handle(() -> PostResponse.from(postUseCase.getPost(id)));
  }

  public BaseResponse<Void> updatePost(long id, String title, String content, String category) {
    return handleVoid(
        () -> postUseCase.updatePost(id, title, content, Category.from(category)));
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
