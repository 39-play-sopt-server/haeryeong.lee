package org.sopt.server.adapter.in.auth;

import org.sopt.server.adapter.in.auth.dto.LoginRequest;
import org.sopt.server.adapter.in.auth.dto.SignupRequest;
import org.sopt.server.adapter.in.common.BaseResponse;
import org.sopt.server.adapter.in.member.dto.MemberResponse;
import org.sopt.server.application.port.in.AuthTokens;
import org.sopt.server.application.port.in.AuthUseCase;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthApi {
  private static final String BEARER_PREFIX = "Bearer ";

  private final AuthUseCase authUseCase;
  private final RefreshTokenCookieProvider refreshTokenCookieProvider;

  public AuthController(
      AuthUseCase authUseCase, RefreshTokenCookieProvider refreshTokenCookieProvider) {
    this.authUseCase = authUseCase;
    this.refreshTokenCookieProvider = refreshTokenCookieProvider;
  }

  @Override
  @PostMapping("/signup")
  @ResponseStatus(HttpStatus.CREATED)
  public BaseResponse<MemberResponse> signup(@RequestBody SignupRequest request) {
    return BaseResponse.success(
        MemberResponse.from(
            authUseCase.signup(request.name(), request.email(), request.password())));
  }

  @Override
  @PostMapping("/login")
  public ResponseEntity<BaseResponse<Void>> login(@RequestBody LoginRequest request) {
    return withTokens(authUseCase.login(request.email(), request.password()));
  }

  @Override
  @PostMapping("/reissue")
  public ResponseEntity<BaseResponse<Void>> reissue(
      @CookieValue(name = RefreshTokenCookieProvider.COOKIE_NAME, required = false)
          String refreshToken) {
    return withTokens(authUseCase.reissue(refreshToken));
  }

  @Override
  @PostMapping("/logout")
  public ResponseEntity<BaseResponse<Void>> logout(
      @CookieValue(name = RefreshTokenCookieProvider.COOKIE_NAME, required = false)
          String refreshToken) {
    authUseCase.logout(refreshToken);
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, refreshTokenCookieProvider.expire().toString())
        .body(BaseResponse.success(null));
  }

  private ResponseEntity<BaseResponse<Void>> withTokens(AuthTokens tokens) {
    return ResponseEntity.ok()
        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + tokens.accessToken())
        .header(
            HttpHeaders.SET_COOKIE,
            refreshTokenCookieProvider.create(tokens.refreshToken()).toString())
        .header(HttpHeaders.CACHE_CONTROL, "no-store")
        .body(BaseResponse.success(null));
  }
}
