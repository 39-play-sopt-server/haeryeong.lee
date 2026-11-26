package org.sopt.server.adapter.in.member;

import org.sopt.server.adapter.in.auth.RefreshTokenCookieProvider;
import org.sopt.server.adapter.in.common.BaseResponse;
import org.sopt.server.adapter.in.member.dto.MemberResponse;
import org.sopt.server.application.port.in.MemberUseCase;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
public class MemberController implements MemberApi {
  private final MemberUseCase memberUseCase;
  private final RefreshTokenCookieProvider refreshTokenCookieProvider;

  public MemberController(
      MemberUseCase memberUseCase, RefreshTokenCookieProvider refreshTokenCookieProvider) {
    this.memberUseCase = memberUseCase;
    this.refreshTokenCookieProvider = refreshTokenCookieProvider;
  }

  @Override
  @GetMapping("/me")
  public BaseResponse<MemberResponse> getMe(@AuthenticationPrincipal Long memberId) {
    return BaseResponse.success(MemberResponse.from(memberUseCase.getMember(memberId)));
  }

  @Override
  @DeleteMapping("/me")
  public ResponseEntity<BaseResponse<Void>> deleteMe(@AuthenticationPrincipal Long memberId) {
    memberUseCase.deleteMember(memberId);
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, refreshTokenCookieProvider.expire().toString())
        .body(BaseResponse.success(null));
  }
}
