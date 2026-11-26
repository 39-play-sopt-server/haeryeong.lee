package org.sopt.server.adapter.in.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record WithdrawRequest(
    @Schema(description = "본인 확인용 비밀번호", example = "password1234") String password) {}
