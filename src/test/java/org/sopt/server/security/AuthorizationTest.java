package org.sopt.server.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationTest {
  private static final String PASSWORD = "password1234";
  private static final String POST_BODY =
      """
      {"title": "제목", "content": "내용", "category": "자유"}
      """;

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;

  private long writerId;
  private long otherId;
  private String writerToken;
  private String otherToken;

  @BeforeEach
  void setUp() throws Exception {
    signup("작성자", "writer@sopt.org");
    signup("다른 회원", "other@sopt.org");
    writerId = memberId("writer@sopt.org");
    otherId = memberId("other@sopt.org");
    writerToken = login("writer@sopt.org");
    otherToken = login("other@sopt.org");
  }

  @AfterEach
  void tearDown() {
    jdbcTemplate.update("delete from refresh_token");
    jdbcTemplate.update("delete from post");
    jdbcTemplate.update("delete from member");
  }

  @Test
  @DisplayName("게시글 작성자는 요청 본문이 아닌 Access Token의 회원으로 저장된다")
  void createPostAsTokenOwner() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/posts")
                .header(HttpHeaders.AUTHORIZATION, bearer(writerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"title": "제목", "content": "내용", "category": "자유", "memberId": %d}
                    """
                        .formatted(otherId)))
        .andExpect(status().isCreated());

    assertEquals(
        writerId, jdbcTemplate.queryForObject("select member_id from post", Long.class));
  }

  @Test
  @DisplayName("작성자가 아닌 회원이 게시글을 수정하거나 삭제하면 403을 응답한다")
  void updateAndDeleteByOtherMember() throws Exception {
    long postId = createPost(writerToken);

    mockMvc
        .perform(
            put("/api/v1/posts/{id}", postId)
                .header(HttpHeaders.AUTHORIZATION, bearer(otherToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(POST_BODY))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("POST-E010"));
    mockMvc
        .perform(
            delete("/api/v1/posts/{id}", postId)
                .header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("POST-E010"));

    mockMvc
        .perform(
            delete("/api/v1/posts/{id}", postId)
                .header(HttpHeaders.AUTHORIZATION, bearer(writerToken)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("회원 탈퇴는 Access Token의 회원만 처리하고 다른 회원 ID로는 탈퇴시킬 수 없다")
  void deleteMe() throws Exception {
    mockMvc
        .perform(
            delete("/api/v1/members/{id}", writerId)
                .header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
        .andExpect(status().is4xxClientError());
    mockMvc
        .perform(get("/api/v1/members/me").header(HttpHeaders.AUTHORIZATION, bearer(writerToken)))
        .andExpect(status().isOk());

    mockMvc
        .perform(delete("/api/v1/members/me").header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
        .andExpect(status().isOk());

    mockMvc
        .perform(get("/api/v1/members/me").header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
        .andExpect(status().isNotFound());
    mockMvc
        .perform(get("/api/v1/members/me").header(HttpHeaders.AUTHORIZATION, bearer(writerToken)))
        .andExpect(status().isOk());
  }

  private void signup(String name, String email) throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"name": "%s", "email": "%s", "password": "%s"}
                    """
                        .formatted(name, email, PASSWORD)))
        .andExpect(status().isCreated());
  }

  private String login(String email) throws Exception {
    return mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email": "%s", "password": "%s"}
                    """
                        .formatted(email, PASSWORD)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getHeader(HttpHeaders.AUTHORIZATION)
        .substring("Bearer ".length());
  }

  private long memberId(String email) {
    return jdbcTemplate.queryForObject("select id from member where email = ?", Long.class, email);
  }

  private long createPost(String token) throws Exception {
    mockMvc
        .perform(
            post("/api/v1/posts")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(POST_BODY))
        .andExpect(status().isCreated());
    return jdbcTemplate.queryForObject("select max(id) from post", Long.class);
  }

  private String bearer(String token) {
    return "Bearer " + token;
  }
}
