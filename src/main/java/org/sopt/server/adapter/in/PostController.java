package org.sopt.server.adapter.in;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sopt.server.application.port.in.PostUseCase;
import org.sopt.server.domain.Category;
import org.sopt.server.domain.exception.BaseException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Post", description = "게시글 API")
@RestController
@RequestMapping("/api/v1/posts")
public class PostController {
  private static final Logger log = Logger.getLogger(PostController.class.getName());

  private final PostUseCase postUseCase;

  public PostController(PostUseCase postUseCase) {
    this.postUseCase = postUseCase;
  }

  public BaseResponse<List<String>> getCategories() {
    return handle(() -> Arrays.stream(Category.values()).map(Category::getLabel).toList());
  }

  @Operation(summary = "게시글 작성")
  @PostMapping
  public BaseResponse<Void> createPost(@RequestBody PostCreateRequest request) {
    return handleVoid(
        () ->
            postUseCase.createPost(
                request.title(),
                request.content(),
                Category.from(request.category()),
                request.author()));
  }

  @Operation(summary = "게시글 목록 조회")
  @GetMapping
  public BaseResponse<List<PostResponse>> getPosts() {
    return handle(() -> postUseCase.getPosts().stream().map(PostResponse::from).toList());
  }

  @Operation(summary = "게시글 상세 조회")
  @GetMapping("/{id}")
  public BaseResponse<PostResponse> getPost(@PathVariable long id) {
    return handle(() -> PostResponse.from(postUseCase.getPost(id)));
  }

  @Operation(summary = "게시글 수정")
  @PutMapping("/{id}")
  public BaseResponse<Void> updatePost(
      @PathVariable long id, @RequestBody PostUpdateRequest request) {
    return handleVoid(
        () ->
            postUseCase.updatePost(
                id, request.title(), request.content(), Category.from(request.category())));
  }

  @Operation(summary = "게시글 삭제")
  @DeleteMapping("/{id}")
  public BaseResponse<Void> deletePost(@PathVariable long id) {
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
