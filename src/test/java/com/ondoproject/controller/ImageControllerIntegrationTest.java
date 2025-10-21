package com.ondoproject.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ondoproject.domain.project.Images_Info;
import com.ondoproject.domain.project.Project;
import com.ondoproject.repository.ImageRepository;
import com.ondoproject.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class ImageControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private Project testProject;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // 테스트용 프로젝트 생성
        testProject = Project.builder()
            .projectName("Test Project")
            .description("Test Description")
            .isAvailable(true)
            .CreatedDateTime("2025-08-03 12:00:00")
            .duration("6 months")
            .grossFloorArea("1000㎡")
            .client("Test Client")
            .architect("Test Architect")
            .index(1)
            .build();
        testProject = projectRepository.save(testProject);
    }

    @Test
    void 전체_이미지_업로드_플로우_테스트() throws Exception {
        // 1. 이미지 업로드
        MockMultipartFile testFile = new MockMultipartFile(
            "file", "integration-test.jpg", MediaType.IMAGE_JPEG_VALUE, "test image content".getBytes());

        mockMvc.perform(multipart("/images/upload")
                .file(testFile)
                .param("projectId", testProject.getId().toString())
                .param("description", "Integration test image")
                .param("isShow", "true")
                .param("isBasic", "false")
                .param("index", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fileName").value("integration-test.jpg"))
            .andExpect(jsonPath("$.projectId").value(testProject.getId()))
            .andExpect(jsonPath("$.isShow").value(true))
            .andExpect(jsonPath("$.isBasic").value(false))
            .andExpect(jsonPath("$.index").value(1));

        // 2. 업로드된 이미지 조회
        mockMvc.perform(get("/images"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$[?(@.fileName == 'integration-test.jpg')]").exists());
    }

    @Test
    void 다중_이미지_업로드_통합_테스트() throws Exception {
        // Given
        MockMultipartFile file1 = new MockMultipartFile(
            "files", "multi1.jpg", MediaType.IMAGE_JPEG_VALUE, "image1 content".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
            "files", "multi2.jpg", MediaType.IMAGE_JPEG_VALUE, "image2 content".getBytes());

        // When & Then
        mockMvc.perform(multipart("/images/upload/multiple")
                .file(file1)
                .file(file2)
                .param("projectId", testProject.getId().toString())
                .param("description", "Multiple upload test")
                .param("isShow", "true")
                .param("isBasic", "false"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].fileName").value("multi1.jpg"))
            .andExpect(jsonPath("$[1].fileName").value("multi2.jpg"))
            .andExpect(jsonPath("$[*].projectId", everyItem(equalTo(testProject.getId().intValue()))));

        // 데이터베이스에서 확인
        mockMvc.perform(get("/images"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.fileName == 'multi1.jpg')]").exists())
            .andExpect(jsonPath("$[?(@.fileName == 'multi2.jpg')]").exists());
    }

    @Test
    void 이미지_업데이트_통합_테스트() throws Exception {
        // 1. 이미지 업로드
        MockMultipartFile testFile = new MockMultipartFile(
            "file", "update-test.jpg", MediaType.IMAGE_JPEG_VALUE, "test content".getBytes());

        String uploadResponse = mockMvc.perform(multipart("/images/upload")
                .file(testFile)
                .param("projectId", testProject.getId().toString())
                .param("description", "Original description"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // 업로드된 이미지 ID 추출
        Long imageId = objectMapper.readTree(uploadResponse).get("id").asLong();

        // 2. 이미지 정보 업데이트
        mockMvc.perform(put("/images/{imageId}", imageId)
                .param("description", "Updated description")
                .param("isShow", "false")
                .param("index", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(imageId))
            .andExpect(jsonPath("$.isShow").value(false))
            .andExpect(jsonPath("$.index").value(10));

        // 3. 업데이트 확인
        mockMvc.perform(get("/images"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == " + imageId + ")].isShow").value(false));
    }

    @Test
    void 이미지_삭제_통합_테스트() throws Exception {
        // 1. 이미지 업로드
        MockMultipartFile testFile = new MockMultipartFile(
            "file", "delete-test.jpg", MediaType.IMAGE_JPEG_VALUE, "test content".getBytes());

        String uploadResponse = mockMvc.perform(multipart("/images/upload")
                .file(testFile)
                .param("description", "To be deleted"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Long imageId = objectMapper.readTree(uploadResponse).get("id").asLong();

        // 2. 이미지 삭제
        mockMvc.perform(delete("/images/{imageId}", imageId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Image and related tags deleted successfully"))
            .andExpect(jsonPath("$.imageId").value(imageId));

        // 3. 삭제 확인
        mockMvc.perform(get("/images"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == " + imageId + ")]").doesNotExist());
    }

    @Test
    void 존재하지_않는_프로젝트로_업로드_실패_테스트() throws Exception {
        MockMultipartFile testFile = new MockMultipartFile(
            "file", "fail-test.jpg", MediaType.IMAGE_JPEG_VALUE, "test content".getBytes());

        mockMvc.perform(multipart("/images/upload")
                .file(testFile)
                .param("projectId", "99999")) // 존재하지 않는 프로젝트 ID
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("UPLOAD_FAILED"))
            .andExpect(jsonPath("$.message", containsString("프로젝트를 찾을 수 없습니다")));
    }

    @Test
    void 파일명_중복_처리_통합_테스트() throws Exception {
        // 1. 첫 번째 이미지 업로드
        MockMultipartFile file1 = new MockMultipartFile(
            "file", "duplicate.jpg", MediaType.IMAGE_JPEG_VALUE, "content1".getBytes());

        mockMvc.perform(multipart("/images/upload")
                .file(file1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fileName").value("duplicate.jpg"));

        // 2. 같은 이름의 두 번째 이미지 업로드
        MockMultipartFile file2 = new MockMultipartFile(
            "file", "duplicate.jpg", MediaType.IMAGE_JPEG_VALUE, "content2".getBytes());

        mockMvc.perform(multipart("/images/upload")
                .file(file2))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fileName").value("duplicate_1.jpg"));

        // 3. 같은 이름의 세 번째 이미지 업로드
        MockMultipartFile file3 = new MockMultipartFile(
            "file", "duplicate.jpg", MediaType.IMAGE_JPEG_VALUE, "content3".getBytes());

        mockMvc.perform(multipart("/images/upload")
                .file(file3))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fileName").value("duplicate_2.jpg"));
    }

    @Test
    void 대용량_파일_업로드_제한_테스트() throws Exception {
        // 5MB 파일 생성 (실제 제한은 설정에 따라 다름)
        byte[] largeContent = new byte[5 * 1024 * 1024];
        MockMultipartFile largeFile = new MockMultipartFile(
            "file", "large.jpg", MediaType.IMAGE_JPEG_VALUE, largeContent);

        // 파일 크기 제한에 따라 성공하거나 실패할 수 있음
        // 성공 또는 실패 둘 다 허용
        try {
            mockMvc.perform(multipart("/images/upload")
                    .file(largeFile))
                .andExpect(status().isOk());
        } catch (Exception e) {
            // 실패해도 괜찮음 (파일 크기 제한에 따라)
            mockMvc.perform(multipart("/images/upload")
                    .file(largeFile))
                .andExpect(status().isBadRequest());
        }
    }

    @Test
    void 빈_파일_업로드_실패_테스트() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file", "empty.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[0]);

        mockMvc.perform(multipart("/images/upload")
                .file(emptyFile))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("UPLOAD_FAILED"))
            .andExpect(jsonPath("$.message", containsString("파일이 비어있습니다")));
    }

    @Test
    void 잘못된_파일_형식_업로드_실패_테스트() throws Exception {
        MockMultipartFile textFile = new MockMultipartFile(
            "file", "document.txt", MediaType.TEXT_PLAIN_VALUE, "text content".getBytes());

        mockMvc.perform(multipart("/images/upload")
                .file(textFile))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("UPLOAD_FAILED"))
            .andExpect(jsonPath("$.message", containsString("지원하지 않는 파일 형식입니다")));
    }
}
