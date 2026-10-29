package org.sopt.server.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.server.application.port.out.MemberRepository;
import org.sopt.server.domain.Category;
import org.sopt.server.domain.Member;
import org.sopt.server.domain.exception.BaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class PostServiceConcurrencyTest {
  private static final int THREAD_COUNT = 32;
  private static final int REQUEST_COUNT = 10_000;

  @Autowired private PostService postService;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private MemberRepository memberRepository;

  private long memberId;

  @BeforeEach
  void setUp() {
    memberId = memberRepository.save(new Member("작성자")).getId();
  }

  @AfterEach
  void tearDown() {
    // 소프트 삭제된 데이터는 리포지토리로 조회되지 않으므로 테이블을 직접 비운다
    jdbcTemplate.update("delete from post");
    jdbcTemplate.update("delete from member");
  }

  @Test
  @DisplayName("여러 사용자가 동시에 게시글을 작성해도 모든 게시글이 저장된다")
  void createConcurrently() throws Exception {
    runConcurrently(() -> postService.createPost("제목", "내용", Category.FREE, memberId));

    assertEquals(REQUEST_COUNT, postService.getPosts(0, 1).getTotalElements());
  }

  @Test
  @DisplayName("여러 사용자가 같은 게시글을 동시에 조회해도 조회수가 누락되지 않는다")
  void getConcurrently() throws Exception {
    long id = postService.createPost("제목", "내용", Category.FREE, memberId);

    runConcurrently(() -> postService.getPost(id));

    assertEquals(REQUEST_COUNT, postService.getPosts(0, 1).getContent().get(0).getViewCount());
  }

  @Test
  @DisplayName("조회 중에 게시글이 삭제되어도 삭제된 게시글이 다시 저장되지 않는다")
  void getWhileDelete() throws Exception {
    List<Long> ids = new ArrayList<>();
    for (int i = 0; i < 1_000; i++) {
      ids.add(postService.createPost("제목", "내용", Category.FREE, memberId));
    }

    // 게시글마다 조회와 삭제를 동시에 시도한다
    List<Runnable> tasks = new ArrayList<>();
    for (long id : ids) {
      tasks.add(() -> postService.getPost(id));
      tasks.add(() -> postService.deletePost(id));
    }
    runConcurrently(tasks);

    assertEquals(0, postService.getPosts(0, 1).getTotalElements());
  }

  private void runConcurrently(Runnable task) throws Exception {
    List<Runnable> tasks = new ArrayList<>();
    for (int i = 0; i < REQUEST_COUNT; i++) {
      tasks.add(task);
    }
    runConcurrently(tasks);
  }

  private void runConcurrently(List<Runnable> tasks) throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
    CountDownLatch start = new CountDownLatch(1);
    List<Future<?>> futures = new ArrayList<>();
    for (Runnable task : tasks) {
      futures.add(
          executor.submit(
              () -> {
                start.await();
                try {
                  task.run();
                } catch (BaseException ignored) {
                  // 이미 삭제된 게시글에 대한 요청은 실패해도 된다
                }
                return null;
              }));
    }
    start.countDown();
    for (Future<?> future : futures) {
      future.get();
    }
    executor.shutdown();
  }
}
