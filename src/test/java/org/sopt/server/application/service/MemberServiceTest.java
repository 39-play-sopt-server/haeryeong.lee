package org.sopt.server.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.server.application.port.out.MemberRepository;
import org.sopt.server.application.port.out.PostRepository;
import org.sopt.server.application.port.out.RefreshTokenRepository;
import org.sopt.server.domain.Category;
import org.sopt.server.domain.Member;
import org.sopt.server.domain.RefreshToken;
import org.sopt.server.domain.exception.BaseException;
import org.sopt.server.domain.exception.MemberErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class MemberServiceTest {
  private static final long NOT_EXIST_ID = Long.MAX_VALUE;

  private final AtomicInteger emailSequence = new AtomicInteger();

  @Autowired private MemberService memberService;
  @Autowired private PostService postService;
  @Autowired private MemberRepository memberRepository;
  @Autowired private PostRepository postRepository;
  @Autowired private RefreshTokenRepository refreshTokenRepository;
  @Autowired private JdbcTemplate jdbcTemplate;

  @AfterEach
  void tearDown() {
    // 소프트 삭제된 데이터는 리포지토리로 조회되지 않으므로 테이블을 직접 비운다
    jdbcTemplate.update("delete from refresh_token");
    jdbcTemplate.update("delete from post");
    jdbcTemplate.update("delete from member");
  }

  @Test
  @DisplayName("회원을 id로 조회한다")
  void getMember() {
    long id = saveMember("이해령").getId();

    Member member = memberService.getMember(id);

    assertEquals("이해령", member.getName());
  }

  @Test
  @DisplayName("삭제된 회원은 조회할 수 없다")
  void getDeletedMember() {
    long id = saveMember("이해령").getId();
    memberService.deleteMember(id);

    BaseException e = assertThrows(BaseException.class, () -> memberService.getMember(id));

    assertEquals(MemberErrorCode.MEMBER_NOT_FOUND, e.getErrorCode());
  }

  @Test
  @DisplayName("회원을 삭제하면 회원이 작성한 게시글도 함께 삭제된다")
  void deleteWithPosts() {
    long author = saveMember("작성자").getId();
    long other = saveMember("다른 회원").getId();
    postService.createPost("제목", "내용", Category.FREE, author);
    postService.createPost("제목", "내용", Category.FREE, author);
    long otherPost = postService.createPost("제목", "내용", Category.FREE, other);

    memberService.deleteMember(author);

    assertFalse(memberRepository.existsById(author));
    assertEquals(1, postRepository.count());
    assertTrue(postRepository.existsById(otherPost));
  }

  @Test
  @DisplayName("회원을 삭제해도 회원과 게시글 데이터는 남고 같은 삭제 시각이 기록된다")
  void deleteIsSoft() {
    long author = saveMember("작성자").getId();
    postService.createPost("제목", "내용", Category.FREE, author);
    postService.createPost("제목", "내용", Category.FREE, author);

    memberService.deleteMember(author);

    LocalDateTime memberDeletedAt =
        jdbcTemplate.queryForObject(
            "select deleted_at from member where id = ?", LocalDateTime.class, author);
    List<LocalDateTime> postDeletedAts =
        jdbcTemplate.queryForList(
            "select deleted_at from post where member_id = ?", LocalDateTime.class, author);
    assertNotNull(memberDeletedAt);
    assertEquals(List.of(memberDeletedAt, memberDeletedAt), postDeletedAts);
  }

  @Test
  @DisplayName("삭제된 회원으로는 게시글을 작성할 수 없다")
  void createPostByDeletedMember() {
    long author = saveMember("작성자").getId();
    memberService.deleteMember(author);

    BaseException e =
        assertThrows(
            BaseException.class,
            () -> postService.createPost("제목", "내용", Category.FREE, author));

    assertEquals(MemberErrorCode.MEMBER_NOT_FOUND, e.getErrorCode());
  }

  @Test
  @DisplayName("이미 삭제된 회원은 다시 삭제할 수 없다")
  void deleteDeleted() {
    long id = saveMember("작성자").getId();
    memberService.deleteMember(id);

    BaseException e = assertThrows(BaseException.class, () -> memberService.deleteMember(id));

    assertEquals(MemberErrorCode.MEMBER_NOT_FOUND, e.getErrorCode());
  }

  @Test
  @DisplayName("존재하지 않는 회원을 삭제하면 예외가 발생한다")
  void deleteNotFound() {
    BaseException e =
        assertThrows(BaseException.class, () -> memberService.deleteMember(NOT_EXIST_ID));

    assertEquals(MemberErrorCode.MEMBER_NOT_FOUND, e.getErrorCode());
  }

  @Test
  @DisplayName("최대 길이의 이름도 DB에 그대로 저장된다")
  void createWithMaxLength() {
    String name = "가".repeat(Member.MAX_NAME_LENGTH);

    Member member = saveMember(name);

    assertEquals(name, memberRepository.findById(member.getId()).orElseThrow().getName());
  }

  @Test
  @DisplayName("회원을 삭제하면 그 회원의 Refresh Token만 모두 폐기되고 삭제 시각은 그대로 기록된다")
  void deleteRevokesRefreshTokens() {
    long member = saveMember("탈퇴 회원").getId();
    long other = saveMember("다른 회원").getId();
    saveRefreshToken(member);
    saveRefreshToken(member);
    saveRefreshToken(other);

    memberService.deleteMember(member);

    assertEquals(
        0,
        jdbcTemplate.queryForObject(
            "select count(*) from refresh_token where member_id = ?", Long.class, member));
    assertEquals(
        1,
        jdbcTemplate.queryForObject(
            "select count(*) from refresh_token where member_id = ?", Long.class, other));
    assertNotNull(
        jdbcTemplate.queryForObject(
            "select deleted_at from member where id = ?", LocalDateTime.class, member));
  }

  @Test
  @DisplayName("회원을 삭제하면 이름, 이메일, 비밀번호가 익명화되어 저장된다")
  void deleteAnonymizesPersonalInformation() {
    Member member = saveMember("이해령");
    String email = member.getEmail();

    memberService.deleteMember(member.getId());

    Map<String, Object> row =
        jdbcTemplate.queryForMap(
            "select name, email, password from member where id = ?", member.getId());
    assertEquals("탈퇴한 회원", row.get("name"));
    assertEquals("deleted_" + member.getId() + "@withdrawn.invalid", row.get("email"));
    assertEquals("", row.get("password"));
    assertEquals(
        0L,
        jdbcTemplate.queryForObject(
            "select count(*) from member where email = ?", Long.class, email));
  }

  private void saveRefreshToken(long memberId) {
    refreshTokenRepository.save(
        new RefreshToken(memberId, UUID.randomUUID().toString(), Instant.now().plusSeconds(60)));
  }

  private Member saveMember(String name) {
    return memberRepository.save(
        new Member(name, "member" + emailSequence.incrementAndGet() + "@sopt.org", "password"));
  }
}
