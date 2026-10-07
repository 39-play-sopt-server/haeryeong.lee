package org.sopt.client;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;
import org.sopt.server.adapter.in.PostResponse;

public class PostView {
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private final Scanner scanner = new Scanner(System.in);

  public void printMenu() {
    System.out.println("\n=== 게시판 ===");
    System.out.println("1. 게시글 작성");
    System.out.println("2. 게시글 목록 조회");
    System.out.println("3. 게시글 단건 조회");
    System.out.println("4. 게시글 수정");
    System.out.println("5. 게시글 삭제");
    System.out.println("6. 종료");
  }

  public int readCommand() {
    System.out.print("선택: ");
    return Integer.parseInt(scanner.nextLine());
  }

  public String readCategory(List<String> categories) {
    System.out.print("카테고리(" + String.join("/", categories) + "): ");
    return scanner.nextLine();
  }

  public String readAuthor() {
    System.out.print("작성자: ");
    return scanner.nextLine();
  }

  public String readTitle() {
    System.out.print("제목: ");
    return scanner.nextLine();
  }

  public String readContent() {
    System.out.print("내용: ");
    return scanner.nextLine();
  }

  public long readPostId(String message) {
    System.out.print(message);
    return Long.parseLong(scanner.nextLine());
  }

  public void printPost(PostResponse post) {
    System.out.println("\n=== 게시글 ===");
    System.out.println("번호: " + post.id());
    System.out.println("카테고리: " + post.category());
    System.out.println("제목: " + post.title());
    System.out.println("작성자: " + post.author());
    System.out.println("작성일: " + post.createdAt().format(DATE_TIME_FORMATTER));
    System.out.println("수정일: " + post.updatedAt().format(DATE_TIME_FORMATTER));
    System.out.println("내용: " + post.content());
  }

  public void printPostSummary(PostResponse post) {
    System.out.println(
        post.id() + ". [" + post.category() + "] " + post.title() + " - " + post.author());
  }

  public void printMessage(String message) {
    System.out.println(message);
  }
}
