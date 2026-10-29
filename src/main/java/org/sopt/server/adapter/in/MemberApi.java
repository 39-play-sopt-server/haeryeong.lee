package org.sopt.server.adapter.in;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Member", description = "회원 API")
public interface MemberApi {
  @Operation(summary = "회원 생성")
  @ApiResponse(responseCode = "201", description = "생성 성공")
  @ApiResponse(responseCode = "400", description = "이름 누락 또는 길이 초과")
  BaseResponse<MemberResponse> createMember(MemberCreateRequest request);

  @Operation(summary = "회원 삭제", description = "회원이 작성한 게시글도 함께 삭제됩니다.")
  @ApiResponse(responseCode = "200", description = "삭제 성공")
  @ApiResponse(responseCode = "404", description = "존재하지 않는 회원")
  BaseResponse<Void> deleteMember(@Parameter(description = "회원 ID", example = "1") long id);
}
