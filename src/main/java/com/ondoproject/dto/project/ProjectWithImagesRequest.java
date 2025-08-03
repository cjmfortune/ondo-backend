package com.ondoproject.dto.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectWithImagesRequest {
    private String projectName;
    private String description;
    private boolean isAvailable;
    private String duration;
    private String grossFloorArea;
    private String client;
    private String architect;
    private int index;
    private List<Long> imageIds; // 연결할 이미지 ID 목록
}
