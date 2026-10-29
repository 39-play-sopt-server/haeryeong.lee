package org.sopt.server.adapter.in;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostCreateRequest(
    @Schema(description = "제목", example = "2차 세미나 후기") String title,
    @Schema(description = "내용", example = "Spring Boot로 게시판 API를 만들어봤어요.") String content,
    @Schema(description = "카테고리 (자유/질문/정보/공지)", example = "자유") String category,
    @Schema(description = "작성자 회원 ID", example = "1") Long memberId) {}
