package com.ondoproject.controller;

import com.ondoproject.dto.WorkResponse;
import com.ondoproject.service.WorkService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/works")
@RequiredArgsConstructor
public class WorkController {

    private final WorkService workService;

    @Operation(summary = "전체 작품 조회",
            description = "전체 작품을 조회합니다.")
    @GetMapping
    public List<WorkResponse> getAll() {
        return workService.getAllWorks();
    }

    @Operation(summary = "작품 업로드",
            description = "작품을 업로드합니다.")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadWork(
            @RequestParam("image") MultipartFile image,
            @RequestParam("title") String title,
            @RequestParam("description") String description) {

        workService.createWorkItem(image, title, description);
        return ResponseEntity.ok("Work uploaded successfully!");
    }

    @Operation(summary = "작품 업데이트", description = "작품 업데이트.")
    @PostMapping(value = "/{id}/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateWork(
            @PathVariable Long id,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description",required = false) String description) {

        workService.updateWorkItem(id, image, title, description);
        return ResponseEntity.ok("Work updated successfully!");
    }
    @Operation(summary = "작품 삭제", description = "작품 삭제.")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteWork(@PathVariable Long id) {
        workService.deleteWorkItem(id);
        return ResponseEntity.ok("Work deleted successfully!");
    }



}