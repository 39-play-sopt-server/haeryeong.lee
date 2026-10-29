package org.sopt.server.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.server.application.port.out.MemberRepository;
import org.sopt.server.application.port.out.PostRepository;
import org.sopt.server.domain.Category;
import org.sopt.server.domain.Member;
import org.sopt.server.domain.exception.BaseException;
import org.sopt.server.domain.exception.MemberErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MemberServiceTest {
  private static final long NOT_EXIST_ID = Long.MAX_VALUE;

  @Autowired private MemberService memberService;
  @Autowired private PostService postService;
  @Autowired private MemberRepository memberRepository;
  @Autowired private PostRepository postRepository;

  @AfterEach
  void tearDown() {
    postRepository.deleteAll();
    memberRepository.deleteAll();
  }

  @Test
  @DisplayName("회원을 생성하면 id가 부여되어 저장된다")
  void create() {
    Member member = memberService.createMember("이해령");

    assertTrue(memberRepository.existsById(member.getId()));
    assertEquals("이해령", member.getName());
  }

  @Test
  @DisplayName("이름이 비어있으면 회원이 저장되지 않는다")
  void createInvalid() {
    BaseException e = assertThrows(BaseException.class, () -> memberService.createMember(" "));

    assertEquals(MemberErrorCode.NAME_REQUIRED, e.getErrorCode());
    assertEquals(0, memberRepository.count());
  }

  @Test
  @DisplayName("회원을 삭제하면 회원이 작성한 게시글도 함께 삭제된다")
  void deleteWithPosts() {
    long author = memberService.createMember("작성자").getId();
    long other = memberService.createMember("다른 회원").getId();
    postService.createPost("제목", "내용", Category.FREE, author);
    postService.createPost("제목", "내용", Category.FREE, author);
    long otherPost = postService.createPost("제목", "내용", Category.FREE, other);

    memberService.deleteMember(author);

    assertFalse(memberRepository.existsById(author));
    assertEquals(1, postRepository.count());
    assertTrue(postRepository.existsById(otherPost));
  }

  @Test
  @DisplayName("존재하지 않는 회원을 삭제하면 예외가 발생한다")
  void deleteNotFound() {
    BaseException e =
        assertThrows(BaseException.class, () -> memberService.deleteMember(NOT_EXIST_ID));

    assertEquals(MemberErrorCode.MEMBER_NOT_FOUND, e.getErrorCode());
  }
}
