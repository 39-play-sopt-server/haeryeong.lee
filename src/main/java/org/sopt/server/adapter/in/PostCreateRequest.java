package org.sopt.server.adapter.in;

public record PostCreateRequest(String title, String content, String category, String author) {}
