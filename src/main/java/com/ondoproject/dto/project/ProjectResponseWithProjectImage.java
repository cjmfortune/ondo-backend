package com.ondoproject.dto.project;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponseWithProjectImage {
    private Long id;
    private String projectName;
    private String description;
    private boolean isAvailable;
    private String CreatedDateTime;
    private String duration;
    private String grossFloorArea;
    private String client;
    private String architect;
    private int index;
    private String projectImageUrl;
}
