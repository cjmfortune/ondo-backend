package com.ondoproject.service.image;

import com.ondoproject.domain.project.Images_Info;
import com.ondoproject.domain.project.Project;
import com.ondoproject.dto.image.ImageUploadResponse;
import com.ondoproject.repository.ImageRepository;
import com.ondoproject.repository.ImageTagRepository;
import com.ondoproject.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ImageTagRepository imageTagRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ImageService imageService;

    @TempDir
    Path tempDir;

    private MockMultipartFile testImageFile;
    private Project testProject;
    private Images_Info testImage;

    @BeforeEach
    void setUp() {
        testImageFile = new MockMultipartFile(
            "file",
            "test-image.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        testProject = Project.builder()
            .id(1L)
            .projectName("Test Project")
            .description("Test Description")
            .build();

        testImage = Images_Info.builder()
            .id(1L)
            .ImageName("test-image.jpg")
            .imageURL("/uploads/test-image.jpg")
            .project(testProject)
            .projectDescription("Test image")
            .isShow(true)
            .isBasic(false)
            .index(0)
            .createDateTime("2025-08-03 12:00:00")
            .build();
    }

    @Test
    void 단일_이미지_업로드_성공_프로젝트_있음() {
        // Given
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(imageRepository.save(any(Images_Info.class))).thenReturn(testImage);

        // When
        ImageUploadResponse response = imageService.uploadImage(
            testImageFile, 1L, "Test image", true, false, 0);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFileName()).isEqualTo("test-image.jpg");
        assertThat(response.getProjectId()).isEqualTo(1L);
        assertThat(response.isShow()).isTrue();
        assertThat(response.isBasic()).isFalse();
        assertThat(response.getIndex()).isEqualTo(0);

        verify(projectRepository).findById(1L);
        verify(imageRepository).save(any(Images_Info.class));
    }

    @Test
    void 단일_이미지_업로드_성공_프로젝트_없음() {
        // Given
        Images_Info imageWithoutProject = Images_Info.builder()
            .id(2L)
            .ImageName("test-image.jpg")
            .imageURL("/uploads/test-image.jpg")
            .projectDescription("Test image")
            .isShow(true)
            .isBasic(false)
            .index(0)
            .createDateTime("2025-08-03 12:00:00")
            .build();

        when(imageRepository.save(any(Images_Info.class))).thenReturn(imageWithoutProject);

        // When
        ImageUploadResponse response = imageService.uploadImage(
            testImageFile, null, "Test image", true, false, 0);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getProjectId()).isNull();
        
        verify(projectRepository, never()).findById(any());
        verify(imageRepository).save(any(Images_Info.class));
    }

    @Test
    void 이미지_업로드_실패_빈_파일() {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file", "empty.jpg", "image/jpeg", new byte[0]);

        // When & Then
        assertThatThrownBy(() -> imageService.uploadImage(emptyFile, null, null, true, false, 0))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("파일이 비어있습니다");

        verify(imageRepository, never()).save(any());
    }

    @Test
    void 이미지_업로드_실패_잘못된_파일_형식() {
        // Given
        MockMultipartFile textFile = new MockMultipartFile(
            "file", "document.txt", "text/plain", "text content".getBytes());

        // When & Then
        assertThatThrownBy(() -> imageService.uploadImage(textFile, null, null, true, false, 0))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("지원하지 않는 파일 형식입니다");

        verify(imageRepository, never()).save(any());
    }

    @Test
    void 이미지_업로드_실패_존재하지_않는_프로젝트() {
        // Given
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> imageService.uploadImage(testImageFile, 999L, null, true, false, 0))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("프로젝트를 찾을 수 없습니다");

        verify(projectRepository).findById(999L);
        verify(imageRepository, never()).save(any());
    }

    @Test
    void 다중_이미지_업로드_성공() {
        // Given
        MockMultipartFile file1 = new MockMultipartFile(
            "files", "image1.jpg", "image/jpeg", "image1 content".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
            "files", "image2.jpg", "image/jpeg", "image2 content".getBytes());
        MultipartFile[] files = {file1, file2};

        Images_Info image1 = Images_Info.builder()
            .id(1L)
            .ImageName("image1.jpg")
            .imageURL("/uploads/image1.jpg")
            .createDateTime("2025-08-03 12:00:00")
            .build();

        Images_Info image2 = Images_Info.builder()
            .id(2L)
            .ImageName("image2.jpg")
            .imageURL("/uploads/image2.jpg")
            .createDateTime("2025-08-03 12:00:00")
            .build();

        when(imageRepository.save(any(Images_Info.class)))
            .thenReturn(image1)
            .thenReturn(image2);

        // When
        List<ImageUploadResponse> responses = imageService.uploadMultipleImages(
            files, null, "Multiple images", true, false);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getFileName()).isEqualTo("image1.jpg");
        assertThat(responses.get(1).getFileName()).isEqualTo("image2.jpg");

        verify(imageRepository, times(2)).save(any(Images_Info.class));
    }

    @Test
    void 다중_이미지_업로드_실패_빈_배열() {
        // Given
        MultipartFile[] emptyFiles = {};

        // When & Then
        assertThatThrownBy(() -> imageService.uploadMultipleImages(
            emptyFiles, null, null, true, false))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("업로드할 파일이 없습니다");

        verify(imageRepository, never()).save(any());
    }

    @Test
    void 이미지_삭제_성공() throws IOException {
        // Given
        Path testFile = tempDir.resolve("test-image.jpg");
        Files.write(testFile, "test content".getBytes());
        
        Images_Info imageToDelete = Images_Info.builder()
            .id(1L)
            .ImageName("test-image.jpg")
            .imageURL(testFile.toString())
            .build();

        when(imageRepository.findById(1L)).thenReturn(Optional.of(imageToDelete));

        // When
        imageService.deleteImageWithCascade(1L);

        // Then
        verify(imageRepository).findById(1L);
        verify(imageRepository).delete(imageToDelete);
    }

    @Test
    void 이미지_삭제_실패_존재하지_않는_이미지() {
        // Given
        when(imageRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> imageService.deleteImageWithCascade(999L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("이미지를 찾을 수 없습니다");

        verify(imageRepository).findById(999L);
        verify(imageRepository, never()).delete(any());
    }

    @Test
    void 이미지_정보_업데이트_성공() {
        // Given
        Images_Info existingImage = Images_Info.builder()
            .id(1L)
            .ImageName("old-image.jpg")
            .imageURL("/uploads/old-image.jpg")
            .projectDescription("Old description")
            .isShow(true)
            .isBasic(false)
            .index(0)
            .createDateTime("2025-08-03 12:00:00")
            .build();

        Images_Info updatedImage = Images_Info.builder()
            .id(1L)
            .ImageName("new-image.jpg")
            .imageURL("/uploads/old-image.jpg")
            .project(testProject)
            .projectDescription("New description")
            .isShow(false)
            .isBasic(true)
            .index(5)
            .createDateTime("2025-08-03 12:00:00")
            .build();

        when(imageRepository.findById(1L)).thenReturn(Optional.of(existingImage));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(imageRepository.save(any(Images_Info.class))).thenReturn(updatedImage);

        // When
        ImageUploadResponse response = imageService.updateImage(
            1L, 1L, "New description", false, true, 5, "new-image.jpg");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFileName()).isEqualTo("new-image.jpg");
        assertThat(response.getProjectId()).isEqualTo(1L);
        assertThat(response.isShow()).isFalse();
        assertThat(response.isBasic()).isTrue();
        assertThat(response.getIndex()).isEqualTo(5);

        verify(imageRepository).findById(1L);
        verify(projectRepository).findById(1L);
        verify(imageRepository).save(any(Images_Info.class));
    }

    @Test
    void 이미지_정보_업데이트_실패_존재하지_않는_이미지() {
        // Given
        when(imageRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> imageService.updateImage(
            999L, null, null, null, null, null, null))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("이미지를 찾을 수 없습니다");

        verify(imageRepository).findById(999L);
        verify(imageRepository, never()).save(any());
    }

    @Test
    void 허용된_파일_형식_검증() {
        // Given - 허용된 이미지 형식들
        String[] allowedTypes = {"image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"};
        
        for (String contentType : allowedTypes) {
            MockMultipartFile validFile = new MockMultipartFile(
                "file", "test." + contentType.split("/")[1], contentType, "test content".getBytes());
            
            when(imageRepository.save(any(Images_Info.class))).thenReturn(testImage);

            // When & Then - 예외가 발생하지 않아야 함
            assertThatCode(() -> imageService.uploadImage(validFile, null, null, true, false, 0))
                .doesNotThrowAnyException();
        }
    }

    @Test
    void 허용되지_않은_파일_형식_검증() {
        // Given - 허용되지 않은 파일 형식들
        String[] disallowedTypes = {"text/plain", "application/pdf", "video/mp4", "audio/mp3"};
        
        for (String contentType : disallowedTypes) {
            MockMultipartFile invalidFile = new MockMultipartFile(
                "file", "test.txt", contentType, "test content".getBytes());

            // When & Then - 예외가 발생해야 함
            assertThatThrownBy(() -> imageService.uploadImage(invalidFile, null, null, true, false, 0))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("지원하지 않는 파일 형식입니다");
        }
    }
}
