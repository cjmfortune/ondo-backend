package com.ondoproject.dto.news;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class NewsResponse {
    private Long id;
    private String title;
    private String content;
    private String imageURL;
    private String createDateTime;
    private String updateDateTime;

}
