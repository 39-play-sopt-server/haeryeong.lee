package org.sopt.client;

import java.util.List;
import org.sopt.server.adapter.in.PostController;
import org.sopt.server.adapter.in.PostResponse;
import org.sopt.server.config.AppConfig;
import org.sopt.server.domain.exception.BaseException;

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
      } catch (BaseException e) {
        view.printMessage(e.getMessage());
      } catch (NumberFormatException e) {
        view.printMessage("숫자를 입력해주세요.");
      }
    }
  }

  private void createPost() {
    String category = view.readCategory();
    String author = view.readAuthor();
    String title = view.readTitle();
    String content = view.readContent();
    controller.createPost(title, content, category, author);
    view.printMessage("게시글이 작성되었어요!");
  }

  private void readPosts() {
    view.printMessage("\n=== 게시글 목록 ===");

    List<PostResponse> posts = controller.getPosts();
    if (posts.isEmpty()) {
      view.printMessage("게시글이 없습니다.");
      return;
    }
    for (int i = 0; i < posts.size(); i++) {
      view.printPostSummary(i + 1, posts.get(i));
    }
  }

  private void readPost() {
    if (hasNoPosts()) {
      return;
    }

    int number = view.readPostNumber("조회할 게시글 번호: ");

    view.printPost(controller.getPost(number));
  }

  private void updatePost() {
    if (hasNoPosts()) {
      return;
    }

    int number = view.readPostNumber("수정할 게시글 번호: ");

    controller.getPost(number);

    String newCategory = view.readCategory();
    String newTitle = view.readTitle();
    String newContent = view.readContent();

    controller.updatePost(number, newTitle, newContent, newCategory);
    view.printMessage("게시글이 수정되었어요!");
  }

  private void deletePost() {
    if (hasNoPosts()) {
      return;
    }

    int number = view.readPostNumber("삭제할 게시글 번호: ");

    controller.deletePost(number);
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
