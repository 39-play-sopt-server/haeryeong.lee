package org.sopt.server.adapter.in.member;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.server.adapter.in.common.BaseResponse;
import org.sopt.server.adapter.in.member.dto.MemberResponse;
import org.sopt.server.adapter.in.member.dto.WithdrawRequest;
import org.springframework.http.ResponseEntity;

@Tag(name = "Member", description = "회원 API")
public interface MemberApi {
  @Operation(summary = "내 정보 조회", description = "Access Token의 회원 정보를 조회합니다.")
  @ApiResponse(responseCode = "200", description = "조회 성공")
  @ApiResponse(responseCode = "401", description = "토큰 없음, 위조 또는 만료")
  @ApiResponse(responseCode = "404", description = "탈퇴한 회원")
  BaseResponse<MemberResponse> getMe(@Parameter(hidden = true) Long memberId);

  @Operation(summary = "회원 탈퇴", description = "Access Token의 회원을 탈퇴 처리하며, 작성한 게시글과 Refresh Token도 함께 삭제됩니다.")
  @ApiResponse(responseCode = "200", description = "탈퇴 성공")
  @ApiResponse(responseCode = "400", description = "비밀번호 불일치")
  @ApiResponse(responseCode = "401", description = "토큰 없음, 위조 또는 만료")
  @ApiResponse(responseCode = "404", description = "이미 탈퇴한 회원")
  ResponseEntity<BaseResponse<Void>> deleteMe(
      @Parameter(hidden = true) Long memberId, WithdrawRequest request);
}
