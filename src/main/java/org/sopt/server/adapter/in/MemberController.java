package org.sopt.server.adapter.in;

import org.sopt.server.application.port.in.MemberUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
public class MemberController implements MemberApi {
  private final MemberUseCase memberUseCase;

  public MemberController(MemberUseCase memberUseCase) {
    this.memberUseCase = memberUseCase;
  }

  @Override
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public BaseResponse<MemberResponse> createMember(@RequestBody MemberCreateRequest request) {
    return BaseResponse.success(MemberResponse.from(memberUseCase.createMember(request.name())));
  }

  @Override
  @DeleteMapping("/{id}")
  public BaseResponse<Void> deleteMember(@PathVariable long id) {
    memberUseCase.deleteMember(id);
    return BaseResponse.success(null);
  }
}
