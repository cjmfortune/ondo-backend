package com.ondoproject.controller;

import com.ondoproject.domain.project.ImageTag;
import com.ondoproject.domain.project.Images_Info;
import com.ondoproject.domain.tag.Tag;
import com.ondoproject.dto.imagetag.ImageTagResponse;
import com.ondoproject.service.ImageTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/image-tags")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ImageTagController {

    private final ImageTagService imageTagService;

    /**
     * 이미지와 태그 연결
     * POST /api/image-tags/link?imageId=93&tagId=41
     */
    @PostMapping("/link")
    public ResponseEntity<ImageTagResponse> linkImageToTag(
            @RequestParam Long imageId,
            @RequestParam Long tagId) {
        try {
            ImageTag imageTag = imageTagService.linkImageToTag(imageId, tagId);
            ImageTagResponse response = new ImageTagResponse(
                imageTag.getId(),
                imageTag.getImagesInfo().getId(),
                imageTag.getTag().getId(),
                imageTag.getTag().getTagName(),
                imageTag.getCreateDateTime()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 이미지와 태그 연결 해제
     * DELETE /api/image-tags/unlink?imageId=93&tagId=41
     */
    @DeleteMapping("/unlink")
    public ResponseEntity<Void> unlinkImageFromTag(
            @RequestParam Long imageId,
            @RequestParam Long tagId) {
        try {
            imageTagService.unlinkImageFromTag(imageId, tagId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 특정 이미지의 모든 태그 조회
     * GET /api/image-tags/image/93/tags
     */
    @GetMapping("/image/{imageId}/tags")
    public ResponseEntity<List<Tag>> getTagsByImageId(@PathVariable Long imageId) {
        try {
            List<Tag> tags = imageTagService.getTagsByImageId(imageId);
            return ResponseEntity.ok(tags);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 특정 태그의 모든 이미지 조회
     * GET /api/image-tags/tag/41/images
     */
    @GetMapping("/tag/{tagId}/images")
    public ResponseEntity<List<Images_Info>> getImagesByTagId(@PathVariable Long tagId) {
        try {
            List<Images_Info> images = imageTagService.getImagesByTagId(tagId);
            return ResponseEntity.ok(images);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 모든 이미지-태그 연결 조회
     * GET /api/image-tags
     */
    @GetMapping
    public ResponseEntity<List<ImageTagResponse>> getAllImageTags() {
        List<ImageTag> imageTags = imageTagService.getAllImageTags();
        List<ImageTagResponse> responses = imageTags.stream()
            .map(imageTag -> new ImageTagResponse(
                imageTag.getId(),
                imageTag.getImagesInfo().getId(),
                imageTag.getTag().getId(),
                imageTag.getTag().getTagName(),
                imageTag.getCreateDateTime()
            ))
            .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * 특정 이미지의 모든 태그 연결 삭제
     * DELETE /api/image-tags/image/93/tags
     */
    @DeleteMapping("/image/{imageId}/tags")
    public ResponseEntity<Void> deleteAllTagsFromImage(@PathVariable Long imageId) {
        try {
            imageTagService.deleteAllTagsFromImage(imageId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 특정 태그의 모든 이미지 연결 삭제
     * DELETE /api/image-tags/tag/41/images
     */
    @DeleteMapping("/tag/{tagId}/images")
    public ResponseEntity<Void> deleteAllImagesFromTag(@PathVariable Long tagId) {
        try {
            imageTagService.deleteAllImagesFromTag(tagId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
