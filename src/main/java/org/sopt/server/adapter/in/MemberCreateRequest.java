package org.sopt.server.adapter.in;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberCreateRequest(@Schema(description = "이름", example = "이해령") String name) {}
