package com.ondoproject.service.image;

import com.ondoproject.domain.project.Images_Info;
import com.ondoproject.domain.project.Project;
import com.ondoproject.dto.image.ImagesDTO;
import com.ondoproject.dto.image.ImageUploadResponse;
import com.ondoproject.repository.ImageRepository;
import com.ondoproject.repository.ImageTagRepository;
import com.ondoproject.repository.ProjectRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ImageService {
    private final ImageRepository imageRepository;
    private final ImageTagRepository imageTagRepository;
    private final ProjectRepository projectRepository;
    
    // 업로드 디렉토리 설정
    private static final String UPLOAD_DIR = "/app/uploads/";
    private static final String BASE_URL = "/uploads/";

    public List<ImagesDTO> getAllImages() {
        // 모든 이미지 조회 (isShow 값에 상관없이, index 순으로 정렬)
        var images = imageRepository.findAllByOrderByIndexAsc();
        
        // 모든 이미지의 ID 리스트 생성
        var imageIds = images.stream()
                .map(image -> image.getId())
                .collect(Collectors.toList());
        
        // 모든 이미지의 태그를 한 번에 조회하여 Map으로 그룹핑 (N+1 문제 해결)
        var imageTagsMap = imageTagRepository.findByImageIds(imageIds).stream()
                .collect(Collectors.groupingBy(
                    imageTag -> imageTag.getImagesInfo().getId(),
                    Collectors.mapping(
                        imageTag -> ImagesDTO.TagInfo.builder()
                                .id(imageTag.getTag().getId())
                                .tagName(imageTag.getTag().getTagName())
                                .createDateTime(imageTag.getTag().getCreateDateTime())
                                .build(),
                        Collectors.toList()
                    )
                ));
        
        return images.stream()
                .map(image -> {
                    var project = image.getProject();
                    var tags = imageTagsMap.getOrDefault(image.getId(), List.of());
                    
                    return ImagesDTO.builder()
                            .id(image.getId())
                            .imageURL(image.getImageURL())
                            .fileName(image.getImageName())
                            .createDateTime(image.getCreateDateTime())
                            .description(project != null ? project.getDescription() : null)
                            .projectName(project != null ? project.getProjectName() : null)
                            .index(image.getIndex())
                            .isShow(image.isShow())
                            .isBasic(image.isBasic())
                            .tags(tags)
                            .build();
                }).toList();
    }

    public ImageUploadResponse uploadImage(MultipartFile file, Long projectId, String description, 
                                         boolean isShow, boolean isBasic, int index) {
        try {
            // 파일 유효성 검사
            validateImageFile(file);
            
            // 업로드 디렉토리 생성
            createUploadDirectory();
            
            // 고유한 파일명 생성
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            
            // 파일 저장 경로
            Path filePath = Paths.get(UPLOAD_DIR + uniqueFileName);
            Files.write(filePath, file.getBytes());
            
            // 프로젝트 조회 (있는 경우)
            Project project = null;
            if (projectId != null) {
                project = projectRepository.findById(projectId).orElse(null);
            }
            
            // 현재 시간
            String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            // Images_Info 엔티티 생성 및 저장
            Images_Info imageInfo = Images_Info.builder()
                    .ImageName(uniqueFileName)
                    .imageURL(BASE_URL + uniqueFileName)
                    .isShow(isShow)
                    .isBasic(isBasic)
                    .index(index)
                    .projectDescription(description)
                    .createDateTime(currentDateTime)
                    .project(project)
                    .build();
            
            Images_Info savedImage = imageRepository.save(imageInfo);
            
            // 응답 생성
            return ImageUploadResponse.builder()
                    .id(savedImage.getId())
                    .fileName(uniqueFileName)
                    .originalFileName(originalFileName)
                    .imageURL(savedImage.getImageURL())
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .createDateTime(currentDateTime)
                    .isShow(isShow)
                    .isBasic(isBasic)
                    .index(index)
                    .projectId(projectId)
                    .projectName(project != null ? project.getProjectName() : null)
                    .message("Image uploaded successfully")
                    .build();
                    
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    public List<ImageUploadResponse> uploadMultipleImages(MultipartFile[] files, Long projectId, 
                                                        String description, boolean isShow, boolean isBasic) {
        List<ImageUploadResponse> responses = new ArrayList<>();
        
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            // 각 파일의 인덱스를 자동으로 설정
            ImageUploadResponse response = uploadImage(file, projectId, description, isShow, isBasic, i);
            responses.add(response);
        }
        
        return responses;
    }

    @Transactional
    public void deleteImage(Long imageId) {
        Images_Info imageInfo = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));
        
        try {
            // 1. 먼저 관련된 ImageTag 레코드들을 삭제 (외래키 제약 조건 해결)
            int deletedTagCount = imageTagRepository.deleteByImagesInfoId(imageId);
            System.out.println("Deleted " + deletedTagCount + " image tags for image ID: " + imageId);
            
            // 2. 변경사항을 즉시 반영하여 외래키 제약 조건 문제 방지
            imageTagRepository.flush();
            
            // 3. 데이터베이스에서 Images_Info 삭제
            imageRepository.delete(imageInfo);
            imageRepository.flush();
            
            // 4. 파일 시스템에서 파일 삭제 (DB 삭제 성공 후)
            Path filePath = Paths.get(UPLOAD_DIR + imageInfo.getImageName());
            boolean fileDeleted = Files.deleteIfExists(filePath);
            System.out.println("File deletion result for " + imageInfo.getImageName() + ": " + fileDeleted);
            
        } catch (Exception e) {
            System.err.println("Error deleting image with ID " + imageId + ": " + e.getMessage());
            throw new RuntimeException("Failed to delete image: " + e.getMessage(), e);
        }
    }

    // 대안적인 삭제 메서드 - 더 안전한 방식
    @Transactional
    public void deleteImageSafely(Long imageId) {
        Images_Info imageInfo = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));
        
        try {
            // 1. 관련된 ImageTag들을 먼저 조회
            var imageTags = imageTagRepository.findByImageId(imageId);
            System.out.println("Found " + imageTags.size() + " image tags to delete for image ID: " + imageId);
            
            // 2. 각각의 ImageTag를 개별적으로 삭제
            for (var imageTag : imageTags) {
                imageTagRepository.delete(imageTag);
                System.out.println("Deleted image tag with ID: " + imageTag.getId());
            }
            
            // 3. 변경사항을 즉시 반영
            imageTagRepository.flush();
            
            // 4. Images_Info 삭제
            imageRepository.delete(imageInfo);
            imageRepository.flush();
            
            // 5. 파일 시스템에서 파일 삭제
            Path filePath = Paths.get(UPLOAD_DIR + imageInfo.getImageName());
            boolean fileDeleted = Files.deleteIfExists(filePath);
            System.out.println("File deletion result for " + imageInfo.getImageName() + ": " + fileDeleted);
            
        } catch (Exception e) {
            System.err.println("Error safely deleting image with ID " + imageId + ": " + e.getMessage());
            throw new RuntimeException("Failed to delete image safely: " + e.getMessage(), e);
        }
    }

    private void createUploadDirectory() {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create upload directory: " + e.getMessage(), e);
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        // 파일 크기 제한 (10MB)
        long maxFileSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("File size exceeds maximum limit of 10MB");
        }
        
        // 허용된 파일 타입 검사
        String contentType = file.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
            throw new RuntimeException("Invalid file type. Only JPG, JPEG, PNG, GIF, WEBP files are allowed");
        }
    }

    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/jpg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/gif") ||
               contentType.equals("image/webp");
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    // Cascade를 활용한 삭제 메서드 - 가장 간단한 방식
    @Transactional
    public void deleteImageWithCascade(Long imageId) {
        Images_Info imageInfo = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));
        
        try {
            // Cascade 설정으로 인해 ImageTag들이 자동으로 삭제됨
            imageRepository.delete(imageInfo);
            imageRepository.flush();
            
            // 파일 시스템에서 파일 삭제
            Path filePath = Paths.get(UPLOAD_DIR + imageInfo.getImageName());
            boolean fileDeleted = Files.deleteIfExists(filePath);
            System.out.println("File deletion result for " + imageInfo.getImageName() + ": " + fileDeleted);
            
        } catch (Exception e) {
            System.err.println("Error deleting image with cascade for ID " + imageId + ": " + e.getMessage());
            throw new RuntimeException("Failed to delete image with cascade: " + e.getMessage(), e);
        }
    }

    public ImageUploadResponse updateImage(Long imageId, Long projectId, String description, 
                                         Boolean isShow, Boolean isBasic, Integer index, String imageName) {
        Images_Info imageInfo = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));
        
        // 프로젝트 조회 (있는 경우)
        Project project = null;
        if (projectId != null) {
            project = projectRepository.findById(projectId).orElse(null);
            imageInfo.setProject(project);
        }
        
        // 필드 업데이트 (null이 아닌 경우에만)
        if (description != null) {
            imageInfo.setProjectDescription(description);
        }
        if (isShow != null) {
            imageInfo.setShow(isShow);
        }
        if (isBasic != null) {
            imageInfo.setBasic(isBasic);
        }
        if (index != null) {
            imageInfo.setIndex(index);
        }
        if (imageName != null && !imageName.trim().isEmpty()) {
            // imageName만 업데이트 (imageURL은 변경하지 않음)
            imageInfo.setImageName(imageName);
        }
        
        Images_Info updatedImage = imageRepository.save(imageInfo);
        
        return ImageUploadResponse.builder()
                .id(updatedImage.getId())
                .fileName(updatedImage.getImageName())
                .imageURL(updatedImage.getImageURL())
                .createDateTime(updatedImage.getCreateDateTime())
                .isShow(updatedImage.isShow())
                .isBasic(updatedImage.isBasic())
                .index(updatedImage.getIndex())
                .projectId(project != null ? project.getId() : null)
                .projectName(project != null ? project.getProjectName() : null)
                .message("Image updated successfully")
                .build();
    }
}
