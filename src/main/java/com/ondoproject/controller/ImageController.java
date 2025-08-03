package com.ondoproject.controller;

import com.ondoproject.dto.image.ImagesDTO;
import com.ondoproject.dto.image.ImageUploadResponse;
import com.ondoproject.dto.image.ErrorResponse;
import com.ondoproject.service.image.ImageService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@AllArgsConstructor
public class ImageController {
    private final ImageService imageService;

    //getAllImages
    @RequestMapping(value = "/images", method = RequestMethod.GET)
    public List<ImagesDTO> getAllImages() {
        return imageService.getAllImages();
    }

    // 단일 이미지 업로드
    @PostMapping("/images/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "projectId", required = false) Long projectId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "isShow", defaultValue = "true") boolean isShow,
            @RequestParam(value = "isBasic", defaultValue = "false") boolean isBasic,
            @RequestParam(value = "index", defaultValue = "0") int index) {
        
        try {
            ImageUploadResponse response = imageService.uploadImage(file, projectId, description, isShow, isBasic, index);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .error("UPLOAD_FAILED")
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // 다중 이미지 업로드
    @PostMapping("/images/upload/multiple")
    public ResponseEntity<?> uploadMultipleImages(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "projectId", required = false) Long projectId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "isShow", defaultValue = "true") boolean isShow,
            @RequestParam(value = "isBasic", defaultValue = "false") boolean isBasic) {
        
        try {
            List<ImageUploadResponse> responses = imageService.uploadMultipleImages(files, projectId, description, isShow, isBasic);
            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .error("MULTIPLE_UPLOAD_FAILED")
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // 이미지 삭제 (관련 ImageTag도 함께 자동 삭제)
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<?> deleteImage(@PathVariable Long imageId) {
        try {
            // Cascade를 활용한 자동 삭제 - ImageTag들이 자동으로 함께 삭제됨
            imageService.deleteImageWithCascade(imageId);
            return ResponseEntity.ok().body("{\"message\": \"Image and related tags deleted successfully\", \"imageId\": " + imageId + "}");
        } catch (RuntimeException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .error("DELETE_FAILED")
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // 기본 삭제 메서드도 제공 (필요시 사용)
    @DeleteMapping("/images/{imageId}/quick")
    public ResponseEntity<?> deleteImageQuick(@PathVariable Long imageId) {
        try {
            imageService.deleteImage(imageId);
            return ResponseEntity.ok().body("{\"message\": \"Image deleted successfully (quick method)\", \"imageId\": " + imageId + "}");
        } catch (RuntimeException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .error("DELETE_FAILED")
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // Cascade를 활용한 삭제 메서드 (가장 간단)
    @DeleteMapping("/images/{imageId}/cascade")
    public ResponseEntity<?> deleteImageWithCascade(@PathVariable Long imageId) {
        try {
            imageService.deleteImageWithCascade(imageId);
            return ResponseEntity.ok().body("{\"message\": \"Image deleted successfully (cascade method)\", \"imageId\": " + imageId + "}");
        } catch (RuntimeException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .error("DELETE_FAILED")
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // 이미지 정보 업데이트
    @PutMapping("/images/{imageId}")
    public ResponseEntity<?> updateImage(
            @PathVariable Long imageId,
            @RequestParam(value = "projectId", required = false) Long projectId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "isShow", required = false) Boolean isShow,
            @RequestParam(value = "isBasic", required = false) Boolean isBasic,
            @RequestParam(value = "index", required = false) Integer index,
            @RequestParam(value = "imageName", required = false) String imageName) {
        
        try {
            ImageUploadResponse response = imageService.updateImage(imageId, projectId, description, isShow, isBasic, index, imageName);
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
}
