package org.sopt.server.adapter.in.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.server.application.port.in.AuthUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenCleanupScheduler {
  private final AuthUseCase authUseCase;

  @Scheduled(cron = "${jwt.refresh-token-cleanup-cron}", zone = "Asia/Seoul")
  public void deleteExpiredRefreshTokens() {
    int deleted = authUseCase.deleteExpiredRefreshTokens();
    log.info("[RefreshTokenCleanup] deleted={}", deleted);
  }
}
