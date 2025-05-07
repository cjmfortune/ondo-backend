package com.ondoproject.service.member;

import com.ondoproject.dto.member.MemberResponse;
import com.ondoproject.repository.MemberRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class MemberService {

    private MemberRepository memberRepository;

    public List<MemberResponse> getAllMembers() {
        return memberRepository.findAll().stream()
                .map(member -> MemberResponse.builder()
                        .id(member.getId())
                        .title(member.getTitle())
                        .name(member.getName())
                        .description(member.getDescription())
                        .build())
                .toList();
    }
}
