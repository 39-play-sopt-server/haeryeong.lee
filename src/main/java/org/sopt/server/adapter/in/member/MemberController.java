package org.sopt.server.adapter.in.member;

import org.sopt.server.adapter.in.common.BaseResponse;
import org.sopt.server.adapter.in.member.dto.MemberResponse;
import org.sopt.server.application.port.in.MemberUseCase;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
public class MemberController implements MemberApi {
  private final MemberUseCase memberUseCase;

  public MemberController(MemberUseCase memberUseCase) {
    this.memberUseCase = memberUseCase;
  }

  @Override
  @GetMapping("/me")
  public BaseResponse<MemberResponse> getMe(@AuthenticationPrincipal Long memberId) {
    return BaseResponse.success(MemberResponse.from(memberUseCase.getMember(memberId)));
  }

  @Override
  @DeleteMapping("/{id}")
  public BaseResponse<Void> deleteMember(@PathVariable long id) {
    memberUseCase.deleteMember(id);
    return BaseResponse.success(null);
  }
}
