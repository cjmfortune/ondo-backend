package com.ondoproject.dto.image;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadResponse {
    private Long id;
    private String fileName;
    private String imageURL;
    private String originalFileName;
    private long fileSize;
    private String contentType;
    private String createDateTime;
    private boolean isShow;
    private boolean isBasic;
    private int index;
    private Long projectId;
    private String projectName;
    private String message;
}
