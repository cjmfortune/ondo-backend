package com.ondoproject.dto.about;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AboutResponse {
    private long id;
    private String description;
    private String description2;
}
