package org.sopt.client;

import java.util.List;
import org.sopt.server.adapter.in.BaseResponse;
import org.sopt.server.adapter.in.PostController;
import org.sopt.server.adapter.in.PostCreateRequest;
import org.sopt.server.adapter.in.PostResponse;
import org.sopt.server.adapter.in.PostUpdateRequest;
import org.sopt.server.config.AppConfig;

public class Main {
  private final PostView view;
  private final PostController controller;

  public Main(PostView view, PostController controller) {
    this.view = view;
    this.controller = controller;
  }

  public static void main(String[] args) {
    new Main(new PostView(), AppConfig.postController()).run();
  }

  private void run() {
    while (true) {
      view.printMenu();
      try {
        int command = view.readCommand();
        switch (command) {
          case 1 -> createPost();
          case 2 -> readPosts();
          case 3 -> readPost();
          case 4 -> updatePost();
          case 5 -> deletePost();
          case 6 -> {
            view.printMessage("프로그램을 종료합니다.");
            return;
          }
          default -> view.printMessage("잘못된 입력입니다.");
        }
      } catch (NumberFormatException e) {
        view.printMessage("숫자를 입력해주세요.");
      }
    }
  }

  private void createPost() {
    BaseResponse<List<String>> categories = controller.getCategories();
    if (isFailure(categories)) {
      return;
    }

    String category = view.readCategory(categories.data());
    String author = view.readAuthor();
    String title = view.readTitle();
    String content = view.readContent();
    PostCreateRequest request = new PostCreateRequest(title, content, category, author);
    if (isFailure(controller.createPost(request))) {
      return;
    }
    view.printMessage("게시글이 작성되었어요!");
  }

  private void readPosts() {
    view.printMessage("\n=== 게시글 목록 ===");

    BaseResponse<List<PostResponse>> response = controller.getPosts();
    if (isFailure(response)) {
      return;
    }

    List<PostResponse> posts = response.data();
    if (posts.isEmpty()) {
      view.printMessage("게시글이 없습니다.");
      return;
    }
    for (PostResponse post : posts) {
      view.printPostSummary(post);
    }
  }

  private void readPost() {
    if (hasNoPosts()) {
      return;
    }

    long id = view.readPostId("조회할 게시글 번호: ");

    BaseResponse<PostResponse> response = controller.getPost(id);
    if (isFailure(response)) {
      return;
    }
    view.printPost(response.data());
  }

  private void updatePost() {
    if (hasNoPosts()) {
      return;
    }

    long id = view.readPostId("수정할 게시글 번호: ");

    if (isFailure(controller.getPost(id))) {
      return;
    }

    BaseResponse<List<String>> categories = controller.getCategories();
    if (isFailure(categories)) {
      return;
    }

    String newCategory = view.readCategory(categories.data());
    String newTitle = view.readTitle();
    String newContent = view.readContent();

    PostUpdateRequest request = new PostUpdateRequest(newTitle, newContent, newCategory);
    if (isFailure(controller.updatePost(id, request))) {
      return;
    }
    view.printMessage("게시글이 수정되었어요!");
  }

  private void deletePost() {
    if (hasNoPosts()) {
      return;
    }

    long id = view.readPostId("삭제할 게시글 번호: ");

    if (isFailure(controller.deletePost(id))) {
      return;
    }
    view.printMessage("게시글이 삭제되었습니다.");
  }

  private boolean hasNoPosts() {
    BaseResponse<List<PostResponse>> response = controller.getPosts();
    if (isFailure(response)) {
      return true;
    }
    if (response.data().isEmpty()) {
      view.printMessage("게시글이 없습니다.");
      return true;
    }
    return false;
  }

  private boolean isFailure(BaseResponse<?> response) {
    if (response.success()) {
      return false;
    }
    view.printMessage(response.message());
    return true;
  }
}
