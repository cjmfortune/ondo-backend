package com.ondoproject.dto.work;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkResponse {
    private Long id;
    private String title;
    private String imageUrl;
}
