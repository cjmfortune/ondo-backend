package com.ondoproject.dto.tag;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TagResponse {
    private Long id;
    private String tagName;
    private String createDateTime;

    private String description;
    private String color;
}
