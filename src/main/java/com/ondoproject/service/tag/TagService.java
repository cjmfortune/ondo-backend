package com.ondoproject.service.tag;

import com.ondoproject.domain.tag.Tag;
import com.ondoproject.dto.tag.TagCreateRequest;
import com.ondoproject.dto.tag.TagResponse;
import com.ondoproject.dto.tag.TagUpdateRequest;
import com.ondoproject.repository.ImageTagRepository;
import com.ondoproject.repository.TagRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@AllArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final ImageTagRepository imageTagRepository;

    // 모든 태그 조회
    public List<TagResponse> findAll() {
        return tagRepository.findAll().stream()
                .map(tag -> new TagResponse(tag.getId(), tag.getTagName(), tag.getCreateDateTime(), tag.getDescription(), tag.getColor()))
                .toList();
    }

    // 특정 태그 조회
    public TagResponse findById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found with id: " + id));
        return new TagResponse(tag.getId(), tag.getTagName(), tag.getCreateDateTime(), tag.getDescription(), tag.getColor());
    }

    // 태그 생성
    @Transactional
    public TagResponse createTag(TagCreateRequest request) {
        // 태그명 유효성 검사
        if (request.getTagName() == null || request.getTagName().trim().isEmpty()) {
            throw new RuntimeException("Tag name cannot be empty");
        }

        // 중복 태그명 검사 (선택사항)
        String trimmedTagName = request.getTagName().trim();
        if (tagRepository.existsByTagName(trimmedTagName)) {
            throw new RuntimeException("Tag with name '" + trimmedTagName + "' already exists");
        }

        // 현재 시간
        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 태그 생성 (Builder 패턴 사용)
        Tag tag = Tag.builder()
                .tagName(trimmedTagName)
                .createDateTime(currentDateTime)
                .description(request.getDescription())
                .color(request.getColor())
                .build();

        Tag savedTag = tagRepository.save(tag);
        return new TagResponse(savedTag.getId(), savedTag.getTagName(), savedTag.getCreateDateTime(), savedTag.getDescription(), savedTag.getColor());
    }

    // 태그 수정
    @Transactional
    public TagResponse updateTag(Long id, TagUpdateRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found with id: " + id));

        // 태그명 유효성 검사
        if (request.getTagName() == null || request.getTagName().trim().isEmpty()) {
            throw new RuntimeException("Tag name cannot be empty");
        }

        String trimmedTagName = request.getTagName().trim();
        
        // 중복 태그명 검사 (자기 자신 제외)
        if (tagRepository.existsByTagNameAndIdNot(trimmedTagName, id)) {
            throw new RuntimeException("Tag with name '" + trimmedTagName + "' already exists");
        }

        tag.setTagName(trimmedTagName);
        if (request.getDescription() != null) {
            tag.setDescription(request.getDescription());
        }
        if (request.getColor() != null) {
            tag.setColor(request.getColor());
        }
        Tag updatedTag = tagRepository.save(tag);
        
        return new TagResponse(updatedTag.getId(), updatedTag.getTagName(), updatedTag.getCreateDateTime(), updatedTag.getDescription(), updatedTag.getColor());
    }

    // 태그 삭제
    @Transactional
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found with id: " + id));

        // 태그가 이미지에 연결되어 있는지 확인
        boolean hasLinkedImages = hasLinkedImages(id);
        
        if (hasLinkedImages) {
            // 옵션 1: 연결된 이미지가 있으면 삭제를 거부
            // throw new RuntimeException("Cannot delete tag. It is linked to one or more images.");
            
            // 옵션 2: 연결된 ImageTag 관계를 먼저 삭제한 후 태그 삭제 (현재 구현)
            deleteAllImageTagRelations(tag);
        }

        tagRepository.delete(tag);
    }

    // 태그가 이미지에 연결되어 있는지 확인하는 메서드
    public boolean hasLinkedImages(Long tagId) {
        return !imageTagRepository.findByTagId(tagId).isEmpty();
    }

    // 특정 태그와 연결된 모든 ImageTag 관계를 삭제하는 메서드
    @Transactional
    public void deleteAllImageTagRelations(Tag tag) {
        imageTagRepository.deleteByTag(tag);
    }

    // 태그 삭제 전 연결된 이미지 개수를 반환하는 메서드 (정보 제공용)
    public int getLinkedImageCount(Long tagId) {
        return imageTagRepository.findByTagId(tagId).size();
    }

    // 태그 삭제 (강제 삭제 - 연결된 이미지 관계도 함께 삭제)
    @Transactional
    public void forceDeleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found with id: " + id));

        // 연결된 모든 ImageTag 관계 삭제
        imageTagRepository.deleteByTag(tag);
        
        // 태그 삭제
        tagRepository.delete(tag);
    }

    // 태그 삭제 (안전 삭제 - 연결된 이미지가 있으면 삭제 거부)
    @Transactional
    public void safeDeleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found with id: " + id));

        // 태그가 이미지에 연결되어 있는지 확인
        if (hasLinkedImages(id)) {
            int linkedImageCount = getLinkedImageCount(id);
            throw new RuntimeException("Cannot delete tag '" + tag.getTagName() + 
                "'. It is currently linked to " + linkedImageCount + " image(s). " +
                "Please remove all image associations before deleting this tag.");
        }

        tagRepository.delete(tag);
    }
}
