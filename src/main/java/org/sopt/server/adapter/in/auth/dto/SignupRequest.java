package org.sopt.server.adapter.in.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SignupRequest(
    @Schema(description = "이름", example = "이해령") String name,
    @Schema(description = "이메일", example = "sopt@sopt.org") String email,
    @Schema(description = "비밀번호 (8~20자)", example = "password1234") String password) {}
