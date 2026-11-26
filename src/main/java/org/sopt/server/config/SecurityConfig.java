package org.sopt.server.config;

import org.sopt.server.security.JwtAuthenticationEntryPoint;
import org.sopt.server.security.JwtAuthenticationFilter;
import org.sopt.server.security.jwt.AccessTokenResolver;
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
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  private static final String[] PUBLIC_POST_PATHS = {
    "/api/v1/auth/signup", "/api/v1/auth/login", "/api/v1/auth/reissue", "/api/v1/auth/logout"
  };
  private static final String[] PUBLIC_GET_PATHS = {"/api/v1/posts"};
  private static final String POST_DETAIL_REGEX = "^/api/v1/posts/\\d+(\\?.*)?$";
  private static final String[] PUBLIC_PATHS = {
    "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error"
  };

  private final AccessTokenResolver accessTokenResolver;
  private final JwtAuthenticationEntryPoint authenticationEntryPoint;

  public SecurityConfig(
      AccessTokenResolver accessTokenResolver,
      JwtAuthenticationEntryPoint authenticationEntryPoint) {
    this.accessTokenResolver = accessTokenResolver;
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
                    .requestMatchers(RegexRequestMatcher.regexMatcher(HttpMethod.GET, POST_DETAIL_REGEX))
                    .permitAll()
                    .requestMatchers(PUBLIC_PATHS)
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(e -> e.authenticationEntryPoint(authenticationEntryPoint))
        .addFilterBefore(
            new JwtAuthenticationFilter(accessTokenResolver), UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
