package com.ondoproject.dto.project;

import lombok.*;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponse {
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
}
