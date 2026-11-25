package org.sopt.server.adapter.in.member;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.server.adapter.in.common.BaseResponse;
import org.sopt.server.adapter.in.member.dto.MemberResponse;

@Tag(name = "Member", description = "회원 API")
public interface MemberApi {
  @Operation(summary = "내 정보 조회", description = "Access Token의 회원 정보를 조회합니다.")
  @ApiResponse(responseCode = "200", description = "조회 성공")
  @ApiResponse(responseCode = "401", description = "토큰 없음, 위조 또는 만료")
  @ApiResponse(responseCode = "404", description = "탈퇴한 회원")
  BaseResponse<MemberResponse> getMe(@Parameter(hidden = true) Long memberId);

  @Operation(summary = "회원 삭제", description = "회원이 작성한 게시글도 함께 삭제됩니다.")
  @ApiResponse(responseCode = "200", description = "삭제 성공")
  @ApiResponse(responseCode = "404", description = "존재하지 않는 회원")
  BaseResponse<Void> deleteMember(@Parameter(description = "회원 ID", example = "1") long id);
}
