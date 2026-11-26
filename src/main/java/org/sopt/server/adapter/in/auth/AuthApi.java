package org.sopt.server.adapter.in.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.server.adapter.in.auth.dto.LoginRequest;
import org.sopt.server.adapter.in.auth.dto.SignupRequest;
import org.sopt.server.adapter.in.common.BaseResponse;
import org.sopt.server.adapter.in.member.dto.MemberResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "Auth", description = "인증 API")
@SecurityRequirements
public interface AuthApi {
  @Operation(summary = "회원가입")
  @ApiResponse(responseCode = "201", description = "가입 성공")
  @ApiResponse(responseCode = "400", description = "이름, 이메일, 비밀번호 형식 오류")
  @ApiResponse(responseCode = "409", description = "이미 가입된 이메일")
  BaseResponse<MemberResponse> signup(SignupRequest request);

  @Operation(
      summary = "로그인",
      description =
          "Access Token은 Authorization 응답 헤더로, Refresh Token은 HttpOnly 쿠키(refresh_token)로 발급합니다.")
  @ApiResponse(responseCode = "200", description = "로그인 성공")
  @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치")
  ResponseEntity<BaseResponse<Void>> login(LoginRequest request);

  @Operation(
      summary = "토큰 재발급",
      description =
          "refresh_token 쿠키로 Access Token과 Refresh Token을 함께 재발급합니다. 사용한 Refresh Token은 폐기됩니다.")
  @ApiResponse(responseCode = "200", description = "재발급 성공")
  @ApiResponse(responseCode = "401", description = "Refresh Token 없음, 만료, 위조 또는 재사용")
  ResponseEntity<BaseResponse<Void>> reissue(@Parameter(hidden = true) String refreshToken);

  @Operation(summary = "로그아웃", description = "Refresh Token을 폐기하고 쿠키를 삭제합니다.")
  @ApiResponse(responseCode = "200", description = "로그아웃 성공")
  ResponseEntity<BaseResponse<Void>> logout(@Parameter(hidden = true) String refreshToken);
}
