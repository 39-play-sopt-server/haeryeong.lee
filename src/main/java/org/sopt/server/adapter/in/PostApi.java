package org.sopt.server.adapter.in;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(name = "Post", description = "게시글 API")
public interface PostApi {
  @Operation(summary = "게시글 작성")
  @ApiResponse(responseCode = "201", description = "작성 성공")
  @ApiResponse(responseCode = "400", description = "제목/내용/작성자 누락 또는 잘못된 카테고리")
  @ApiResponse(responseCode = "404", description = "존재하지 않는 회원")
  BaseResponse<Void> createPost(PostCreateRequest request);

  @Operation(summary = "게시글 목록 조회")
  @ApiResponse(responseCode = "200", description = "조회 성공")
  BaseResponse<List<PostResponse>> getPosts();

  @Operation(summary = "게시글 상세 조회", description = "조회할 때마다 조회수가 1 증가합니다.")
  @ApiResponse(responseCode = "200", description = "조회 성공")
  @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글")
  BaseResponse<PostResponse> getPost(@Parameter(description = "게시글 ID", example = "1") long id);

  @Operation(summary = "게시글 수정")
  @ApiResponse(responseCode = "200", description = "수정 성공")
  @ApiResponse(responseCode = "400", description = "제목/내용 누락 또는 잘못된 카테고리")
  @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글")
  BaseResponse<Void> updatePost(
      @Parameter(description = "게시글 ID", example = "1") long id, PostUpdateRequest request);

  @Operation(summary = "게시글 삭제")
  @ApiResponse(responseCode = "200", description = "삭제 성공")
  @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글")
  BaseResponse<Void> deletePost(@Parameter(description = "게시글 ID", example = "1") long id);
}
