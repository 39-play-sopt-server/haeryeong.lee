package org.sopt.client;

import java.util.List;
import java.util.Optional;
import org.sopt.server.adapter.in.PostController;
import org.sopt.server.adapter.in.PostResponse;
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
    }
  }

  private void createPost() {
    String title = view.readTitle();
    String content = view.readContent();
    controller.createPost(title, content);
    view.printMessage("게시글이 작성되었습니다.");
  }

  private void readPosts() {
    view.printMessage("\n=== 게시글 목록 ===");

    List<PostResponse> posts = controller.getPosts();
    if (posts.isEmpty()) {
      view.printMessage("게시글이 없습니다.");
      return;
    }
    for (int i = 0; i < posts.size(); i++) {
      view.printMessage((i + 1) + ". " + posts.get(i).title());
    }
  }

  private void readPost() {
    if (hasNoPosts()) {
      return;
    }

    int number = view.readPostNumber("조회할 게시글 번호: ");

    Optional<PostResponse> post = controller.getPost(number);
    if (post.isEmpty()) {
      view.printMessage("존재하지 않는 게시글입니다.");
      return;
    }
    view.printPost(post.get());
  }

  private void updatePost() {
    if (hasNoPosts()) {
      return;
    }

    int number = view.readPostNumber("수정할 게시글 번호: ");

    if (controller.getPost(number).isEmpty()) {
      view.printMessage("존재하지 않는 게시글입니다.");
      return;
    }

    String newTitle = view.readTitle();
    String newContent = view.readContent();

    controller.updatePost(number, newTitle, newContent);

    view.printMessage("게시글이 수정되었습니다.");
  }

  private void deletePost() {
    if (hasNoPosts()) {
      return;
    }

    int number = view.readPostNumber("삭제할 게시글 번호: ");

    if (!controller.deletePost(number)) {
      view.printMessage("존재하지 않는 게시글입니다.");
      return;
    }

    view.printMessage("게시글이 삭제되었습니다.");
  }

  private boolean hasNoPosts() {
    if (controller.getPosts().isEmpty()) {
      view.printMessage("게시글이 없습니다.");
      return true;
    }
    return false;
  }
}
