package org.sopt.server.security.jwt;

import java.util.Optional;

public interface AccessTokenResolver {
  Optional<Long> resolveMemberId(String accessToken);
}
