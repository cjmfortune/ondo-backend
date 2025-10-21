package com.ondoproject.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ondoproject.dto.image.ImageUploadResponse;
import com.ondoproject.service.image.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ImageController.class)
class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageService imageService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMultipartFile testImageFile;
    private ImageUploadResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // 테스트용 이미지 파일 생성
        testImageFile = new MockMultipartFile(
            "file",
            "test-image.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );

        // Mock 응답 객체 생성
        mockResponse = ImageUploadResponse.builder()
            .id(1L)
            .fileName("test-image.jpg")
            .imageURL("/uploads/test-image.jpg")
            .originalFileName("test-image.jpg")
            .fileSize(17L)
            .contentType("image/jpeg")
            .projectId(1L)
            .projectName("Test Project")
            .isShow(true)
            .isBasic(false)
            .index(0)
            .createDateTime("2025-08-03 12:00:00")
            .message("Upload successful")
            .build();
    }

    @Test
    void 단일_이미지_업로드_성공() throws Exception {
        // Given
        when(imageService.uploadImage(any(), eq(1L), eq("Test image"), eq(true), eq(false), eq(0)))
            .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(multipart("/images/upload")
                .file(testImageFile)
                .param("projectId", "1")
                .param("description", "Test image")
                .param("isShow", "true")
                .param("isBasic", "false")
                .param("index", "0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.fileName").value("test-image.jpg"))
            .andExpect(jsonPath("$.imageURL").value("/uploads/test-image.jpg"))
            .andExpect(jsonPath("$.projectId").value(1L))
            .andExpect(jsonPath("$.projectName").value("Test Project"))
            .andExpect(jsonPath("$.isShow").value(true))
            .andExpect(jsonPath("$.isBasic").value(false))
            .andExpect(jsonPath("$.index").value(0));
    }

    @Test
    void 단일_이미지_업로드_필수_파라미터만() throws Exception {
        // Given
        ImageUploadResponse minimalResponse = ImageUploadResponse.builder()
            .id(2L)
            .fileName("minimal-image.jpg")
            .imageURL("/uploads/minimal-image.jpg")
            .originalFileName("minimal-image.jpg")
            .fileSize(17L)
            .contentType("image/jpeg")
            .isShow(true)
            .isBasic(false)
            .index(0)
            .createDateTime("2025-08-03 12:00:00")
            .message("Upload successful")
            .build();

        when(imageService.uploadImage(any(), isNull(), isNull(), eq(true), eq(false), eq(0)))
            .thenReturn(minimalResponse);

        // When & Then
        mockMvc.perform(multipart("/images/upload")
                .file(testImageFile))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(2L))
            .andExpect(jsonPath("$.fileName").value("minimal-image.jpg"))
            .andExpect(jsonPath("$.isShow").value(true))
            .andExpect(jsonPath("$.isBasic").value(false))
            .andExpect(jsonPath("$.index").value(0));
    }

    @Test
    void 다중_이미지_업로드_성공() throws Exception {
        // Given
        MockMultipartFile file1 = new MockMultipartFile(
            "files", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, "image1 content".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
            "files", "image2.jpg", MediaType.IMAGE_JPEG_VALUE, "image2 content".getBytes());

        List<ImageUploadResponse> responses = Arrays.asList(
            ImageUploadResponse.builder()
                .id(1L)
                .fileName("image1.jpg")
                .imageURL("/uploads/image1.jpg")
                .originalFileName("image1.jpg")
                .fileSize(15L)
                .contentType("image/jpeg")
                .projectId(1L)
                .createDateTime("2025-08-03 12:00:00")
                .build(),
            ImageUploadResponse.builder()
                .id(2L)
                .fileName("image2.jpg")
                .imageURL("/uploads/image2.jpg")
                .originalFileName("image2.jpg")
                .fileSize(15L)
                .contentType("image/jpeg")
                .projectId(1L)
                .createDateTime("2025-08-03 12:00:00")
                .build()
        );

        when(imageService.uploadMultipleImages(any(), eq(1L), eq("Multiple images"), eq(true), eq(false)))
            .thenReturn(responses);

        // When & Then
        mockMvc.perform(multipart("/images/upload/multiple")
                .file(file1)
                .file(file2)
                .param("projectId", "1")
                .param("description", "Multiple images")
                .param("isShow", "true")
                .param("isBasic", "false"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].fileName").value("image1.jpg"))
            .andExpect(jsonPath("$[1].id").value(2L))
            .andExpect(jsonPath("$[1].fileName").value("image2.jpg"));
    }

    @Test
    void 이미지_업로드_실패_빈_파일() throws Exception {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file", "empty.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[0]);

        when(imageService.uploadImage(any(), any(), any(), anyBoolean(), anyBoolean(), anyInt()))
            .thenThrow(new RuntimeException("파일이 비어있습니다"));

        // When & Then
        mockMvc.perform(multipart("/images/upload")
                .file(emptyFile))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("UPLOAD_FAILED"))
            .andExpect(jsonPath("$.message").value("파일이 비어있습니다"));
    }

    @Test
    void 이미지_업로드_실패_잘못된_파일_형식() throws Exception {
        // Given
        MockMultipartFile invalidFile = new MockMultipartFile(
            "file", "document.txt", MediaType.TEXT_PLAIN_VALUE, "text content".getBytes());

        when(imageService.uploadImage(any(), any(), any(), anyBoolean(), anyBoolean(), anyInt()))
            .thenThrow(new RuntimeException("지원하지 않는 파일 형식입니다"));

        // When & Then
        mockMvc.perform(multipart("/images/upload")
                .file(invalidFile))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("UPLOAD_FAILED"))
            .andExpect(jsonPath("$.message").value("지원하지 않는 파일 형식입니다"));
    }

    @Test
    void 이미지_삭제_성공() throws Exception {
        // Given
        Long imageId = 1L;

        // When & Then
        mockMvc.perform(delete("/images/{imageId}", imageId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Image and related tags deleted successfully"))
            .andExpect(jsonPath("$.imageId").value(imageId));
    }

    @Test
    void 이미지_삭제_실패_존재하지_않는_이미지() throws Exception {
        // Given
        Long imageId = 999L;
        doThrow(new RuntimeException("이미지를 찾을 수 없습니다"))
            .when(imageService).deleteImageWithCascade(imageId);

        // When & Then
        mockMvc.perform(delete("/images/{imageId}", imageId))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("DELETE_FAILED"))
            .andExpect(jsonPath("$.message").value("이미지를 찾을 수 없습니다"));
    }

    @Test
    void 이미지_정보_업데이트_성공() throws Exception {
        // Given
        Long imageId = 1L;
        ImageUploadResponse updatedResponse = ImageUploadResponse.builder()
            .id(imageId)
            .fileName("updated-image.jpg")
            .imageURL("/uploads/updated-image.jpg")
            .originalFileName("updated-image.jpg")
            .fileSize(20L)
            .contentType("image/jpeg")
            .projectId(2L)
            .projectName("Updated Project")
            .isShow(false)
            .isBasic(true)
            .index(5)
            .createDateTime("2025-08-03 12:00:00")
            .message("Update successful")
            .build();

        when(imageService.updateImage(eq(imageId), eq(2L), eq("Updated description"), 
                                    eq(false), eq(true), eq(5), eq("updated-image.jpg")))
            .thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/images/{imageId}", imageId)
                .param("projectId", "2")
                .param("description", "Updated description")
                .param("isShow", "false")
                .param("isBasic", "true")
                .param("index", "5")
                .param("imageName", "updated-image.jpg"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(imageId))
            .andExpect(jsonPath("$.projectId").value(2L))
            .andExpect(jsonPath("$.projectName").value("Updated Project"))
            .andExpect(jsonPath("$.isShow").value(false))
            .andExpect(jsonPath("$.isBasic").value(true))
            .andExpect(jsonPath("$.index").value(5))
            .andExpect(jsonPath("$.fileName").value("updated-image.jpg"));
    }

    @Test
    void 파일_크기_제한_테스트() throws Exception {
        // Given - 큰 파일 생성 (예: 10MB)
        byte[] largeFileContent = new byte[10 * 1024 * 1024];
        MockMultipartFile largeFile = new MockMultipartFile(
            "file", "large-image.jpg", MediaType.IMAGE_JPEG_VALUE, largeFileContent);

        when(imageService.uploadImage(any(), any(), any(), anyBoolean(), anyBoolean(), anyInt()))
            .thenThrow(new RuntimeException("파일 크기가 너무 큽니다"));

        // When & Then
        mockMvc.perform(multipart("/images/upload")
                .file(largeFile))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("UPLOAD_FAILED"))
            .andExpect(jsonPath("$.message").value("파일 크기가 너무 큽니다"));
    }

    @Test
    void 특수문자_포함_파일명_테스트() throws Exception {
        // Given
        MockMultipartFile specialCharFile = new MockMultipartFile(
            "file", "테스트 이미지 (1).jpg", MediaType.IMAGE_JPEG_VALUE, "test content".getBytes());

        ImageUploadResponse response = ImageUploadResponse.builder()
            .id(1L)
            .fileName("테스트_이미지_1.jpg") // 특수문자가 변환된 파일명
            .imageURL("/uploads/테스트_이미지_1.jpg")
            .originalFileName("테스트 이미지 (1).jpg")
            .fileSize(12L)
            .contentType("image/jpeg")
            .createDateTime("2025-08-03 12:00:00")
            .message("Upload successful")
            .build();

        when(imageService.uploadImage(any(), any(), any(), anyBoolean(), anyBoolean(), anyInt()))
            .thenReturn(response);

        // When & Then
        mockMvc.perform(multipart("/images/upload")
                .file(specialCharFile))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fileName").value("테스트_이미지_1.jpg"));
    }
}
