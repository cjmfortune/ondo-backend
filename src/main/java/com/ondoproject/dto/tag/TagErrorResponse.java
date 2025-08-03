package com.ondoproject.dto.tag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class TagErrorResponse {
    private String error;
    private String message;
    private String timestamp;
}
