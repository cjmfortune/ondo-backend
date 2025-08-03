package com.ondoproject.dto.news;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsCreateRequest {
    
    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
    private String title;
    
    private String contents;
    
    private String imageURL;
    
    private String fileName;
    
    private String fileType;
    
    @Builder.Default
    private Boolean isPublished = true;
    
    @Size(max = 100, message = "작성자는 100자를 초과할 수 없습니다")
    private String author;
    
    @Size(max = 500, message = "요약은 500자를 초과할 수 없습니다")
    private String summary;
}
