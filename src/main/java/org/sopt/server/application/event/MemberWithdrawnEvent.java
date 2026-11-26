package org.sopt.server.application.event;

import java.time.LocalDateTime;

public record MemberWithdrawnEvent(long memberId, LocalDateTime withdrawnAt) {}
