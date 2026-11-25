package org.sopt.server.adapter.in.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.server.adapter.in.auth.dto.LoginRequest;
import org.sopt.server.adapter.in.auth.dto.LoginResponse;
import org.sopt.server.adapter.in.auth.dto.SignupRequest;
import org.sopt.server.adapter.in.common.BaseResponse;
import org.sopt.server.adapter.in.member.dto.MemberResponse;

@Tag(name = "Auth", description = "인증 API")
@SecurityRequirements
public interface AuthApi {
  @Operation(summary = "회원가입")
  @ApiResponse(responseCode = "201", description = "가입 성공")
  @ApiResponse(responseCode = "400", description = "이름, 이메일, 비밀번호 형식 오류")
  @ApiResponse(responseCode = "409", description = "이미 가입된 이메일")
  BaseResponse<MemberResponse> signup(SignupRequest request);

  @Operation(summary = "로그인", description = "성공 시 Access Token을 발급합니다.")
  @ApiResponse(responseCode = "200", description = "로그인 성공")
  @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치")
  BaseResponse<LoginResponse> login(LoginRequest request);
}
