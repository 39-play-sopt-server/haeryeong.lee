package org.sopt.server.adapter.in;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostUpdateRequest(
    @Schema(description = "제목", example = "2차 세미나 후기 (수정)") String title,
    @Schema(description = "내용", example = "Swagger로 API 문서도 붙였어요.") String content,
    @Schema(description = "카테고리 (자유/질문/정보/공지)", example = "정보") String category) {}
