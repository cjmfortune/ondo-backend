package com.ondoproject.dto.author;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthorResponse {
    Long id;
    String title;
    String name;
    String description;
}
