package com.ondoproject.controller;
import com.ondoproject.dto.member.MemberResponse;
import com.ondoproject.service.member.MemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Tag(name = "멤버", description = "멤버 관련 API")
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    //Read
    @RequestMapping(value = "/read", method = RequestMethod.GET)
    public List<MemberResponse> getAllMembers() {
        List<MemberResponse> members = memberService.getAllMembers();
        return members;
    }
}
