package org.sopt.server.adapter.in;

import java.util.Arrays;
import java.util.List;
import org.sopt.server.application.port.in.PostUseCase;
import org.sopt.server.domain.Category;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController implements PostApi {
  private final PostUseCase postUseCase;

  public PostController(PostUseCase postUseCase) {
    this.postUseCase = postUseCase;
  }

  public BaseResponse<List<String>> getCategories() {
    return BaseResponse.success(Arrays.stream(Category.values()).map(Category::getLabel).toList());
  }

  @Override
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public BaseResponse<Void> createPost(@RequestBody PostCreateRequest request) {
    postUseCase.createPost(
        request.title(), request.content(), Category.from(request.category()), request.author());
    return BaseResponse.success(null);
  }

  @Override
  @GetMapping
  public BaseResponse<List<PostResponse>> getPosts() {
    return BaseResponse.success(postUseCase.getPosts().stream().map(PostResponse::from).toList());
  }

  @Override
  @GetMapping("/{id}")
  public BaseResponse<PostResponse> getPost(@PathVariable long id) {
    return BaseResponse.success(PostResponse.from(postUseCase.getPost(id)));
  }

  @Override
  @PutMapping("/{id}")
  public BaseResponse<Void> updatePost(
      @PathVariable long id, @RequestBody PostUpdateRequest request) {
    postUseCase.updatePost(
        id, request.title(), request.content(), Category.from(request.category()));
    return BaseResponse.success(null);
  }

  @Override
  @DeleteMapping("/{id}")
  public BaseResponse<Void> deletePost(@PathVariable long id) {
    postUseCase.deletePost(id);
    return BaseResponse.success(null);
  }
}
