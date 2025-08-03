package com.ondoproject.controller;

import com.ondoproject.dto.image.ErrorResponse;
import com.ondoproject.dto.project.*;
import com.ondoproject.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Tag(name = "프로젝트", description = "프로젝트 관련")
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "전체 프로젝트 조회", description = "전체 프로젝트를 조회합니다.")
    @GetMapping
    public List<ProjectResponseWithProjectImage> getAll() {
        return projectService.getAllProjectsWithImageUrl();
    }

    @Operation(summary = "프로젝트 단일 조회", description = "ID로 특정 프로젝트를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<?> getProjectById(@PathVariable Long id) {
        try {
            Optional<ProjectResponseWithProjectImage> project = projectService.getProjectById(id);
            if (project.isPresent()) {
                return ResponseEntity.ok(project.get());
            } else {
                ErrorResponse errorResponse = ErrorResponse.builder()
                        .error("PROJECT_NOT_FOUND")
                        .message("Project not found with id: " + id)
                        .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
        } catch (Exception e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .error("FETCH_FAILED")
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "프로젝트 생성", description = "새로운 프로젝트를 생성합니다.")
    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody ProjectCreateRequest request) {
        try {
            ProjectResponseWithProjectImage response = projectService.createProject(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .error("CREATE_FAILED")
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @Operation(summary = "프로젝트 업데이트", description = "기존 프로젝트 정보를 업데이트합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(@PathVariable Long id, @RequestBody ProjectUpdateRequest request) {
        try {
            ProjectResponseWithProjectImage response = projectService.updateProject(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .error("UPDATE_FAILED")
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @Operation(summary = "이미지와 함께 프로젝트 생성", description = "이미지들과 함께 새로운 프로젝트를 생성합니다.")
    @PostMapping("/with-images")
    public ResponseEntity<?> createProjectWithImages(@RequestBody ProjectWithImagesRequest request) {
        try {
            ProjectResponseWithProjectImage response = projectService.createProjectWithImages(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .error("CREATE_WITH_IMAGES_FAILED")
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @Operation(summary = "프로젝트 이미지 업데이트", description = "프로젝트에 연결된 이미지들을 업데이트합니다. (기존 이미지 연결 해제 후 새로운 이미지 연결)")
    @PutMapping("/{id}/images")
    public ResponseEntity<?> updateProjectImages(
            @PathVariable Long id, 
            @RequestBody List<Long> imageIds) {
        try {
            projectService.updateProjectImages(id, imageIds);
            return ResponseEntity.ok().body("{\"message\": \"Project images updated successfully\"}");
        } catch (RuntimeException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .error("UPDATE_IMAGES_FAILED")
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @Operation(summary = "프로젝트에 이미지 추가", description = "기존 프로젝트에 새로운 이미지들을 추가합니다.")
    @PostMapping("/{id}/images")
    public ResponseEntity<?> addImagesToProject(
            @PathVariable Long id, 
            @RequestBody List<Long> imageIds) {
        try {
            projectService.addImagesToProject(id, imageIds);
            return ResponseEntity.ok().body("{\"message\": \"Images added to project successfully\"}");
        } catch (RuntimeException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .error("ADD_IMAGES_FAILED")
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @Operation(summary = "프로젝트에서 이미지 제거", description = "프로젝트에서 특정 이미지들의 연결을 해제합니다.")
    @DeleteMapping("/{id}/images")
    public ResponseEntity<?> removeImagesFromProject(
            @PathVariable Long id, 
            @RequestBody List<Long> imageIds) {
        try {
            projectService.removeImagesFromProject(id, imageIds);
            return ResponseEntity.ok().body("{\"message\": \"Images removed from project successfully\"}");
        } catch (RuntimeException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .error("REMOVE_IMAGES_FAILED")
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @Operation(summary = "프로젝트 삭제", description = "프로젝트를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable Long id) {
        try {
            projectService.deleteProjectItem(id);
            return ResponseEntity.ok().body("{\"message\": \"Project deleted successfully\"}");
        } catch (RuntimeException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .error("DELETE_FAILED")
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}
