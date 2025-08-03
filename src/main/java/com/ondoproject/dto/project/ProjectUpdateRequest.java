package com.ondoproject.dto.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUpdateRequest {
    private String projectName;
    private String description;
    private Boolean isAvailable;
    private String duration;
    private String grossFloorArea;
    private String client;
    private String architect;
    private Integer index;
}
