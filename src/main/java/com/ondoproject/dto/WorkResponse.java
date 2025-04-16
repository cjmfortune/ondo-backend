package com.ondoproject.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkResponse {
    private Long id;
    private String title;
    private String imageUrl;
}
