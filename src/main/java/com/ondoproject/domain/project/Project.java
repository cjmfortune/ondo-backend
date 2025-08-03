package com.ondoproject.domain.project;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // auto increment
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
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Images_Info> images;
}
