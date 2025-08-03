package com.ondoproject.service;

import com.ondoproject.domain.project.Images_Info;
import com.ondoproject.domain.project.Project;
import com.ondoproject.dto.project.*;
import com.ondoproject.repository.ImageRepository;
import com.ondoproject.repository.ProjectRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ImageRepository imageRepository;
    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAllByOrderByIndexAsc().stream()
                .map(project -> ProjectResponse.builder()
                        .id(project.getId())
                        .projectName(project.getProjectName())
                        .description(project.getDescription())
                        .isAvailable(project.isAvailable())
                        .CreatedDateTime(project.getCreatedDateTime())
                        .duration(project.getDuration())
                        .grossFloorArea(project.getGrossFloorArea())
                        .client(project.getClient())
                        .architect(project.getArchitect())
                        .index(project.getIndex())
                        .build())
                .toList();
    }

    public List<ProjectResponseWithProjectImage> getAllProjectsWithImageUrl() {
        return projectRepository.findAllByOrderByIndexAsc().stream()
                .map(project -> {
                    String imageUrl = null;
                    if (project.getImages() != null && !project.getImages().isEmpty()) {
                        log.info("프로젝트 ID {}의 대표 이미지: {}", project.getId(), project.getImages().stream().findFirst().map(Images_Info::getId).orElse(null));
                        imageUrl = project.getImages().stream()
                                .findFirst()
                                .map(Images_Info::getImageURL)
                                .orElse(null);
                    }
                    return ProjectResponseWithProjectImage.builder()
                            .id(project.getId())
                            .projectName(project.getProjectName())
                            .description(project.getDescription())
                            .isAvailable(project.isAvailable())
                            .CreatedDateTime(project.getCreatedDateTime())
                            .duration(project.getDuration())
                            .grossFloorArea(project.getGrossFloorArea())
                            .client(project.getClient())
                            .architect(project.getArchitect())
                            .index(project.getIndex())
                            .projectImageUrl(imageUrl)
                            .build();
                })
                .toList();
    }

    public Optional<ProjectResponseWithProjectImage> getProjectById(Long id) {
        return projectRepository.findById(id)
                .map(project -> {
                    String imageUrl = null;
                    if (project.getImages() != null && !project.getImages().isEmpty()) {
                        imageUrl = project.getImages().stream()
                                .findFirst()
                                .map(Images_Info::getImageURL)
                                .orElse(null);
                    }
                    return ProjectResponseWithProjectImage.builder()
                            .id(project.getId())
                            .projectName(project.getProjectName())
                            .description(project.getDescription())
                            .isAvailable(project.isAvailable())
                            .CreatedDateTime(project.getCreatedDateTime())
                            .duration(project.getDuration())
                            .grossFloorArea(project.getGrossFloorArea())
                            .client(project.getClient())
                            .architect(project.getArchitect())
                            .index(project.getIndex())
                            .projectImageUrl(imageUrl)
                            .build();
                });
    }

    @Transactional
    public ProjectResponseWithProjectImage createProject(ProjectCreateRequest request) {
        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        Project project = Project.builder()
                .projectName(request.getProjectName())
                .description(request.getDescription())
                .isAvailable(request.isAvailable())
                .CreatedDateTime(currentDateTime)
                .duration(request.getDuration())
                .grossFloorArea(request.getGrossFloorArea())
                .client(request.getClient())
                .architect(request.getArchitect())
                .index(request.getIndex())
                .build();

        Project savedProject = projectRepository.save(project);
        
        return ProjectResponseWithProjectImage.builder()
                .id(savedProject.getId())
                .projectName(savedProject.getProjectName())
                .description(savedProject.getDescription())
                .isAvailable(savedProject.isAvailable())
                .CreatedDateTime(savedProject.getCreatedDateTime())
                .duration(savedProject.getDuration())
                .grossFloorArea(savedProject.getGrossFloorArea())
                .client(savedProject.getClient())
                .architect(savedProject.getArchitect())
                .index(savedProject.getIndex())
                .projectImageUrl(null)
                .build();
    }

    @Transactional
    public ProjectResponseWithProjectImage updateProject(Long id, ProjectUpdateRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

        // null이 아닌 필드만 업데이트
        if (request.getProjectName() != null) {
            project.setProjectName(request.getProjectName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getIsAvailable() != null) {
            project.setAvailable(request.getIsAvailable());
        }
        if (request.getDuration() != null) {
            project.setDuration(request.getDuration());
        }
        if (request.getGrossFloorArea() != null) {
            project.setGrossFloorArea(request.getGrossFloorArea());
        }
        if (request.getClient() != null) {
            project.setClient(request.getClient());
        }
        if (request.getArchitect() != null) {
            project.setArchitect(request.getArchitect());
        }
        if (request.getIndex() != null) {
            project.setIndex(request.getIndex());
        }

        Project updatedProject = projectRepository.save(project);
        
        String imageUrl = null;
        if (updatedProject.getImages() != null && !updatedProject.getImages().isEmpty()) {
            imageUrl = updatedProject.getImages().stream()
                    .findFirst()
                    .map(Images_Info::getImageURL)
                    .orElse(null);
        }

        return ProjectResponseWithProjectImage.builder()
                .id(updatedProject.getId())
                .projectName(updatedProject.getProjectName())
                .description(updatedProject.getDescription())
                .isAvailable(updatedProject.isAvailable())
                .CreatedDateTime(updatedProject.getCreatedDateTime())
                .duration(updatedProject.getDuration())
                .grossFloorArea(updatedProject.getGrossFloorArea())
                .client(updatedProject.getClient())
                .architect(updatedProject.getArchitect())
                .index(updatedProject.getIndex())
                .projectImageUrl(imageUrl)
                .build();
    }

    @Transactional
    public ProjectResponseWithProjectImage createProjectWithImages(ProjectWithImagesRequest request) {
        // 프로젝트 생성
        ProjectCreateRequest projectRequest = ProjectCreateRequest.builder()
                .projectName(request.getProjectName())
                .description(request.getDescription())
                .isAvailable(request.isAvailable())
                .duration(request.getDuration())
                .grossFloorArea(request.getGrossFloorArea())
                .client(request.getClient())
                .architect(request.getArchitect())
                .index(request.getIndex())
                .build();

        ProjectResponseWithProjectImage projectResponse = createProject(projectRequest);
        
        // 이미지들을 프로젝트에 연결
        if (request.getImageIds() != null && !request.getImageIds().isEmpty()) {
            updateProjectImages(projectResponse.getId(), request.getImageIds());
            
            // 업데이트된 프로젝트 정보 다시 조회
            return getProjectById(projectResponse.getId())
                    .orElseThrow(() -> new RuntimeException("Failed to retrieve updated project"));
        }

        return projectResponse;
    }

    @Transactional
    public void updateProjectImages(Long projectId, List<Long> imageIds) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        // 기존 이미지들의 프로젝트 연결 해제
        if (project.getImages() != null) {
            for (Images_Info image : project.getImages()) {
                image.setProject(null);
                imageRepository.save(image);
            }
        }

        // 새로운 이미지들을 프로젝트에 연결
        if (imageIds != null && !imageIds.isEmpty()) {
            for (Long imageId : imageIds) {
                Images_Info image = imageRepository.findById(imageId)
                        .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));
                image.setProject(project);
                imageRepository.save(image);
            }
        }
    }

    @Transactional
    public void addImagesToProject(Long projectId, List<Long> imageIds) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        if (imageIds != null && !imageIds.isEmpty()) {
            for (Long imageId : imageIds) {
                Images_Info image = imageRepository.findById(imageId)
                        .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));
                image.setProject(project);
                imageRepository.save(image);
            }
        }
    }

    @Transactional
    public void removeImagesFromProject(Long projectId, List<Long> imageIds) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        if (imageIds != null && !imageIds.isEmpty()) {
            for (Long imageId : imageIds) {
                Images_Info image = imageRepository.findById(imageId)
                        .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));
                
                // 해당 이미지가 실제로 이 프로젝트에 속하는지 확인
                if (image.getProject() != null && image.getProject().getId().equals(projectId)) {
                    image.setProject(null);
                    imageRepository.save(image);
                }
            }
        }
    }

    public void deleteProjectItem(Long itemId) {
        var workById = projectRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Work item not found"));
        projectRepository.delete(workById);
    }
}
