package org.sopt.server.adapter.in.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginRequest(
    @Schema(description = "이메일", example = "sopt@sopt.org") String email,
    @Schema(description = "비밀번호", example = "password1234") String password) {}
