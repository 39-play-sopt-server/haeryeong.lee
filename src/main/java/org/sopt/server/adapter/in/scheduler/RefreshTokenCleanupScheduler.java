package org.sopt.server.adapter.in.scheduler;

import java.util.logging.Logger;
import org.sopt.server.application.port.in.AuthUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCleanupScheduler {
  private static final Logger log = Logger.getLogger(RefreshTokenCleanupScheduler.class.getName());

  private final AuthUseCase authUseCase;

  public RefreshTokenCleanupScheduler(AuthUseCase authUseCase) {
    this.authUseCase = authUseCase;
  }

  @Scheduled(cron = "${jwt.refresh-token-cleanup-cron}", zone = "Asia/Seoul")
  public void deleteExpiredRefreshTokens() {
    int deleted = authUseCase.deleteExpiredRefreshTokens();
    log.info("[RefreshTokenCleanup] deleted=" + deleted);
  }
}
