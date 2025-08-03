package com.ondoproject.controller;

import com.ondoproject.dto.tag.TagCreateRequest;
import com.ondoproject.dto.tag.TagErrorResponse;
import com.ondoproject.dto.tag.TagResponse;
import com.ondoproject.dto.tag.TagUpdateRequest;
import com.ondoproject.service.tag.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    // 모든 태그 조회
    @GetMapping
    public ResponseEntity<List<TagResponse>> getAllTags() {
        try {
            List<TagResponse> tags = tagService.findAll();
            return ResponseEntity.ok(tags);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 특정 태그 조회
    @GetMapping("/{id}")
    public ResponseEntity<?> getTagById(@PathVariable Long id) {
        try {
            TagResponse tag = tagService.findById(id);
            return ResponseEntity.ok(tag);
        } catch (RuntimeException e) {
            TagErrorResponse errorResponse = TagErrorResponse.builder()
                    .error("TAG_NOT_FOUND")
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            TagErrorResponse errorResponse = TagErrorResponse.builder()
                    .error("INTERNAL_SERVER_ERROR")
                    .message("An unexpected error occurred")
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 태그 생성
    @PostMapping
    public ResponseEntity<?> createTag(@RequestBody TagCreateRequest request) {
        try {
            TagResponse createdTag = tagService.createTag(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTag);
        } catch (RuntimeException e) {
            TagErrorResponse errorResponse = TagErrorResponse.builder()
                    .error("TAG_CREATION_FAILED")
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            TagErrorResponse errorResponse = TagErrorResponse.builder()
                    .error("INTERNAL_SERVER_ERROR")
                    .message("An unexpected error occurred")
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 태그 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTag(@PathVariable Long id, @RequestBody TagUpdateRequest request) {
        try {
            TagResponse updatedTag = tagService.updateTag(id, request);
            return ResponseEntity.ok(updatedTag);
        } catch (RuntimeException e) {
            TagErrorResponse errorResponse = TagErrorResponse.builder()
                    .error("TAG_UPDATE_FAILED")
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
            
            // 태그를 찾을 수 없는 경우 404, 그 외에는 400
            HttpStatus status = e.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(errorResponse);
        } catch (Exception e) {
            TagErrorResponse errorResponse = TagErrorResponse.builder()
                    .error("INTERNAL_SERVER_ERROR")
                    .message("An unexpected error occurred")
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 태그 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTag(@PathVariable Long id) {
        try {
            tagService.deleteTag(id);
            return ResponseEntity.ok().body("{\"message\": \"Tag deleted successfully\", \"tagId\": " + id + "}");
        } catch (RuntimeException e) {
            TagErrorResponse errorResponse = TagErrorResponse.builder()
                    .error("TAG_DELETE_FAILED")
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
            
            // 태그를 찾을 수 없는 경우 404, 그 외에는 400
            HttpStatus status = e.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(errorResponse);
        } catch (Exception e) {
            TagErrorResponse errorResponse = TagErrorResponse.builder()
                    .error("INTERNAL_SERVER_ERROR")
                    .message("An unexpected error occurred")
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 태그명으로 검색
    @GetMapping("/search")
    public ResponseEntity<?> searchTagByName(@RequestParam String name) {
        try {
            // 이 기능을 위해서는 TagService에 searchByName 메서드를 추가해야 합니다
            // 현재는 간단히 모든 태그를 조회하고 필터링하는 방식으로 구현
            List<TagResponse> allTags = tagService.findAll();
            List<TagResponse> filteredTags = allTags.stream()
                    .filter(tag -> tag.getTagName().toLowerCase().contains(name.toLowerCase()))
                    .toList();
            
            return ResponseEntity.ok(filteredTags);
        } catch (Exception e) {
            TagErrorResponse errorResponse = TagErrorResponse.builder()
                    .error("SEARCH_FAILED")
                    .message("Failed to search tags")
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
