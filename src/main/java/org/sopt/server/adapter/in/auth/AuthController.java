package org.sopt.server.adapter.in.auth;

import org.sopt.server.adapter.in.auth.dto.LoginRequest;
import org.sopt.server.adapter.in.auth.dto.LoginResponse;
import org.sopt.server.adapter.in.auth.dto.SignupRequest;
import org.sopt.server.adapter.in.common.BaseResponse;
import org.sopt.server.adapter.in.member.dto.MemberResponse;
import org.sopt.server.application.port.in.AuthUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthApi {
  private final AuthUseCase authUseCase;

  public AuthController(AuthUseCase authUseCase) {
    this.authUseCase = authUseCase;
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
  public BaseResponse<LoginResponse> login(@RequestBody LoginRequest request) {
    return BaseResponse.success(
        new LoginResponse(authUseCase.login(request.email(), request.password())));
  }
}
