package org.sopt.server.security;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationTest {
  private static final String EMAIL = "sopt@sopt.org";
  private static final String PASSWORD = "password1234";
  private static final String REFRESH_COOKIE = "refresh_token";

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.issuer}")
  private String issuer;

  @AfterEach
  void tearDown() {
    jdbcTemplate.update("delete from refresh_token");
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
    expectUnauthorized(
        mockMvc.perform(
            post("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"title": "제목", "content": "내용", "category": "자유"}
                    """)));
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
    String expired = new JwtProvider(secret, issuer, -1000L, 1000L).createAccessToken(memberId);

    expectUnauthorized(
        mockMvc.perform(
            get("/api/v1/members/me").header(HttpHeaders.AUTHORIZATION, bearer(expired))));
  }

  @Test
  @DisplayName("로그인하면 Access Token은 헤더로, Refresh Token은 HttpOnly 쿠키로 발급한다")
  void loginIssuesTokensByHeaderAndCookie() throws Exception {
    signup();

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
        .andExpect(header().string(HttpHeaders.AUTHORIZATION, startsWith("Bearer ")))
        .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
        .andExpect(jsonPath("$.data").doesNotExist())
        .andExpect(cookie().exists(REFRESH_COOKIE))
        .andExpect(cookie().httpOnly(REFRESH_COOKIE, true))
        .andExpect(cookie().secure(REFRESH_COOKIE, true))
        .andExpect(cookie().sameSite(REFRESH_COOKIE, "Strict"))
        .andExpect(cookie().path(REFRESH_COOKIE, "/api/v1/auth"))
        .andExpect(cookie().maxAge(REFRESH_COOKIE, 14 * 24 * 60 * 60));
  }

  @Test
  @DisplayName("Refresh Token 쿠키로 재발급하면 새 Access Token과 새 Refresh Token 쿠키를 응답한다")
  void reissue() throws Exception {
    signup();
    MvcResult login = loginResult();

    MvcResult reissued =
        reissue(refreshCookie(login))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.AUTHORIZATION, startsWith("Bearer ")))
            .andExpect(cookie().httpOnly(REFRESH_COOKIE, true))
            .andReturn();

    assertNotEquals(refreshCookie(login).getValue(), refreshCookie(reissued).getValue());
    mockMvc
        .perform(
            get("/api/v1/members/me")
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(reissued))))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("이미 사용한 Refresh Token으로 다시 재발급하면 401을 응답한다")
  void reissueWithUsedRefreshToken() throws Exception {
    signup();
    Cookie used = refreshCookie(loginResult());
    reissue(used).andExpect(status().isOk());

    reissue(used)
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("AUTH-E004"));
  }

  @Test
  @DisplayName("Refresh Token 쿠키 없이 재발급하면 401을 응답한다")
  void reissueWithoutCookie() throws Exception {
    mockMvc
        .perform(post("/api/v1/auth/reissue"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("AUTH-E003"));
  }

  @Test
  @DisplayName("Refresh Token은 Access Token 대신 사용할 수 없다")
  void refreshTokenCannotBeUsedAsAccessToken() throws Exception {
    signup();
    String refreshToken = refreshCookie(loginResult()).getValue();

    expectUnauthorized(
        mockMvc.perform(
            get("/api/v1/members/me").header(HttpHeaders.AUTHORIZATION, bearer(refreshToken))));
  }

  @Test
  @DisplayName("로그아웃하면 Refresh Token 쿠키를 삭제하고 해당 토큰으로 재발급할 수 없다")
  void logout() throws Exception {
    signup();
    Cookie refreshCookie = refreshCookie(loginResult());

    mockMvc
        .perform(post("/api/v1/auth/logout").cookie(refreshCookie))
        .andExpect(status().isOk())
        .andExpect(cookie().maxAge(REFRESH_COOKIE, 0))
        .andExpect(cookie().path(REFRESH_COOKIE, "/api/v1/auth"));

    reissue(refreshCookie).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("허용된 Origin의 요청에는 Authorization 응답 헤더를 노출한다")
  void exposeAuthorizationHeaderForCors() throws Exception {
    signup();

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email": "%s", "password": "%s"}
                    """
                        .formatted(EMAIL, PASSWORD)))
        .andExpect(status().isOk())
        .andExpect(
            header()
                .string(
                    HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                    containsString(HttpHeaders.AUTHORIZATION)))
        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
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
    return accessToken(loginResult());
  }

  private MvcResult loginResult() throws Exception {
    return mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email": "%s", "password": "%s"}
                    """
                        .formatted(EMAIL, PASSWORD)))
        .andExpect(status().isOk())
        .andReturn();
  }

  private ResultActions reissue(Cookie refreshCookie) throws Exception {
    return mockMvc.perform(post("/api/v1/auth/reissue").cookie(refreshCookie));
  }

  private String accessToken(MvcResult result) {
    return result.getResponse().getHeader(HttpHeaders.AUTHORIZATION).substring("Bearer ".length());
  }

  private Cookie refreshCookie(MvcResult result) {
    return result.getResponse().getCookie(REFRESH_COOKIE);
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
