package org.sopt.server.config;

import org.sopt.server.security.JwtAuthenticationEntryPoint;
import org.sopt.server.security.JwtAuthenticationFilter;
import org.sopt.server.security.JwtProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  private static final String[] PUBLIC_POST_PATHS = {
    "/api/v1/auth/signup", "/api/v1/auth/login", "/api/v1/auth/reissue", "/api/v1/auth/logout"
  };
  private static final String[] PUBLIC_GET_PATHS = {"/api/v1/posts", "/api/v1/posts/*"};
  private static final String[] PUBLIC_PATHS = {
    "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error"
  };

  private final JwtProvider jwtProvider;
  private final JwtAuthenticationEntryPoint authenticationEntryPoint;

  public SecurityConfig(
      JwtProvider jwtProvider, JwtAuthenticationEntryPoint authenticationEntryPoint) {
    this.jwtProvider = jwtProvider;
    this.authenticationEntryPoint = authenticationEntryPoint;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.POST, PUBLIC_POST_PATHS)
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, PUBLIC_GET_PATHS)
                    .permitAll()
                    .requestMatchers(PUBLIC_PATHS)
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(e -> e.authenticationEntryPoint(authenticationEntryPoint))
        .addFilterBefore(
            new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
