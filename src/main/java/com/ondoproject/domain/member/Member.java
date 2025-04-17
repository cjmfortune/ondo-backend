package com.ondoproject.domain.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member {
    Long id;
    String title;
    String name;
    String description;
}
