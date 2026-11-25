package org.sopt.server.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationTest {
  private static final String EMAIL = "sopt@sopt.org";
  private static final String PASSWORD = "password1234";

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Value("${jwt.secret}")
  private String secret;

  @AfterEach
  void tearDown() {
    jdbcTemplate.update("delete from post");
    jdbcTemplate.update("delete from member");
  }

  @Test
  @DisplayName("회원가입 → 로그인 → 발급받은 토큰으로 내 정보를 조회한다")
  void signupLoginAndGetMe() throws Exception {
    signup()
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.email").value(EMAIL))
        .andExpect(jsonPath("$.data.password").doesNotExist());
    String token = login();

    mockMvc
        .perform(get("/api/v1/members/me").header(HttpHeaders.AUTHORIZATION, bearer(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.name").value("이해령"))
        .andExpect(jsonPath("$.data.email").value(EMAIL));
  }

  @Test
  @DisplayName("비밀번호가 틀리면 로그인 시 401을 응답한다")
  void loginFailed() throws Exception {
    signup();

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email": "%s", "password": "wrong-password"}
                    """
                        .formatted(EMAIL)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("AUTH-E002"));
  }

  @Test
  @DisplayName("이미 가입된 이메일로 가입하면 409를 응답한다")
  void signupDuplicate() throws Exception {
    signup();

    signup().andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("MEMBER-E007"));
  }

  @Test
  @DisplayName("토큰 없이 요청하면 공통 응답 형식으로 401을 응답한다")
  void noToken() throws Exception {
    expectUnauthorized(mockMvc.perform(get("/api/v1/members/me")));
    expectUnauthorized(mockMvc.perform(get("/api/v1/posts")));
  }

  @Test
  @DisplayName("Bearer 형식이 아닌 Authorization 헤더는 401을 응답한다")
  void notBearer() throws Exception {
    signup();
    String token = login();

    expectUnauthorized(
        mockMvc.perform(get("/api/v1/members/me").header(HttpHeaders.AUTHORIZATION, token)));
  }

  @Test
  @DisplayName("서명을 변조한 토큰은 401을 응답한다")
  void tamperedToken() throws Exception {
    signup();
    String token = login();
    int index = token.lastIndexOf('.') + 10;
    char replaced = token.charAt(index) == 'A' ? 'B' : 'A';
    String tampered = token.substring(0, index) + replaced + token.substring(index + 1);

    expectUnauthorized(
        mockMvc.perform(
            get("/api/v1/members/me").header(HttpHeaders.AUTHORIZATION, bearer(tampered))));
  }

  @Test
  @DisplayName("만료된 토큰은 401을 응답한다")
  void expiredToken() throws Exception {
    signup();
    long memberId = jdbcTemplate.queryForObject("select id from member", Long.class);
    String expired = new JwtProvider(secret, -1000L).createAccessToken(memberId);

    expectUnauthorized(
        mockMvc.perform(
            get("/api/v1/members/me").header(HttpHeaders.AUTHORIZATION, bearer(expired))));
  }

  @Test
  @DisplayName("Swagger 문서는 토큰 없이 조회할 수 있다")
  void swaggerIsPublic() throws Exception {
    mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
  }

  private ResultActions signup() throws Exception {
    return mockMvc.perform(
        post("/api/v1/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                {"name": "이해령", "email": "%s", "password": "%s"}
                """
                    .formatted(EMAIL, PASSWORD)));
  }

  private String login() throws Exception {
    String body =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"email": "%s", "password": "%s"}
                        """
                            .formatted(EMAIL, PASSWORD)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return JsonPath.read(body, "$.data.accessToken");
  }

  private String bearer(String token) {
    return "Bearer " + token;
  }

  private void expectUnauthorized(ResultActions result) throws Exception {
    result
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("AUTH-E001"));
  }
}
