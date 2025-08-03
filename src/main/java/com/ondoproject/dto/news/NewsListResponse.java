package com.ondoproject.dto.news;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class NewsListResponse {
    private Long id;
    private String title;
    private String summary;
    private String imageURL;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isPublished;
    private String author;
}
