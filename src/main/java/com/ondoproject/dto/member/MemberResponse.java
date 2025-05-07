package com.ondoproject.dto.member;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
    Long id;
    String title;
    String name;
    String description;
}
