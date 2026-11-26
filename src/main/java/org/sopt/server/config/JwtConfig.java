package org.sopt.server.config;

import java.time.Clock;
import org.sopt.server.security.jwt.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {
  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }
}
