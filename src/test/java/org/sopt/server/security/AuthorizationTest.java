package org.sopt.server.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
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
import org.springframework.test.web.servlet.ResultActions;

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

  @Test
  @DisplayName("게시글 목록과 상세 조회는 토큰 없이도, 유효하지 않은 토큰을 붙여도 조회할 수 있다")
  void readPostsWithoutAuthentication() throws Exception {
    long postId = createPost(writerToken);

    mockMvc.perform(get("/api/v1/posts")).andExpect(status().isOk());
    mockMvc.perform(get("/api/v1/posts/{id}", postId)).andExpect(status().isOk());
    mockMvc
        .perform(get("/api/v1/posts").header(HttpHeaders.AUTHORIZATION, bearer("invalid")))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("게시글 작성·수정·삭제와 회원 탈퇴는 토큰이 없으면 401을 응답한다")
  void writeRequiresAuthentication() throws Exception {
    long postId = createPost(writerToken);

    expectUnauthorized(
        mockMvc.perform(
            post("/api/v1/posts").contentType(MediaType.APPLICATION_JSON).content(POST_BODY)));
    expectUnauthorized(
        mockMvc.perform(
            put("/api/v1/posts/{id}", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(POST_BODY)));
    expectUnauthorized(mockMvc.perform(delete("/api/v1/posts/{id}", postId)));
    expectUnauthorized(mockMvc.perform(delete("/api/v1/members/me")));
  }

  @Test
  @DisplayName("공개 목록에 없는 경로는 auth 하위라도 인증을 요구한다")
  void unknownPathRequiresAuthentication() throws Exception {
    expectUnauthorized(mockMvc.perform(get("/api/v1/auth/login")));
    expectUnauthorized(mockMvc.perform(post("/api/v1/auth/unknown")));
    expectUnauthorized(mockMvc.perform(get("/api/v1/admin")));
  }

  @Test
  @DisplayName("회원 탈퇴 시 Refresh Token 쿠키를 삭제하고 DB의 Refresh Token도 폐기한다")
  void withdrawalExpiresRefreshCookie() throws Exception {
    mockMvc
        .perform(delete("/api/v1/members/me").header(HttpHeaders.AUTHORIZATION, bearer(writerToken)))
        .andExpect(status().isOk())
        .andExpect(cookie().maxAge("refresh_token", 0))
        .andExpect(cookie().path("refresh_token", "/api/v1/auth"));

    assertEquals(
        0L,
        jdbcTemplate.queryForObject(
            "select count(*) from refresh_token where member_id = ?", Long.class, writerId));
  }

  private void expectUnauthorized(ResultActions result) throws Exception {
    result.andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH-E001"));
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
