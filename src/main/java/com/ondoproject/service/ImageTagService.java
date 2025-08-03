package com.ondoproject.service;

import com.ondoproject.domain.project.ImageTag;
import com.ondoproject.domain.project.Images_Info;
import com.ondoproject.domain.tag.Tag;
import com.ondoproject.repository.ImageTagRepository;
import com.ondoproject.repository.ImageRepository;
import com.ondoproject.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ImageTagService {

    private final ImageTagRepository imageTagRepository;
    private final ImageRepository imageRepository;
    private final TagRepository tagRepository;

    /**
     * 이미지와 태그 연결
     */
    public ImageTag linkImageToTag(Long imageId, Long tagId) {
        Images_Info image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));
        
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found with id: " + tagId));

        // 이미 연결되어 있는지 확인
        if (imageTagRepository.existsByImagesInfoAndTag(image, tag)) {
            throw new RuntimeException("Image and Tag are already linked");
        }

        ImageTag imageTag = ImageTag.createImageTag(image, tag);
        return imageTagRepository.save(imageTag);
    }

    /**
     * 이미지와 태그 연결 해제
     */
    public void unlinkImageFromTag(Long imageId, Long tagId) {
        Images_Info image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));
        
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found with id: " + tagId));

        imageTagRepository.deleteByImagesInfoAndTag(image, tag);
    }

    /**
     * 특정 이미지의 모든 태그 조회
     */
    @Transactional(readOnly = true)
    public List<Tag> getTagsByImageId(Long imageId) {
        return imageTagRepository.findByImageId(imageId)
                .stream()
                .map(ImageTag::getTag)
                .collect(Collectors.toList());
    }

    /**
     * 특정 태그의 모든 이미지 조회
     */
    @Transactional(readOnly = true)
    public List<Images_Info> getImagesByTagId(Long tagId) {
        return imageTagRepository.findByTagId(tagId)
                .stream()
                .map(ImageTag::getImagesInfo)
                .collect(Collectors.toList());
    }

    /**
     * 특정 이미지의 모든 태그 연결 삭제
     */
    public void deleteAllTagsFromImage(Long imageId) {
        Images_Info image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));
        
        imageTagRepository.deleteByImagesInfo(image);
    }

    /**
     * 특정 태그의 모든 이미지 연결 삭제
     */
    public void deleteAllImagesFromTag(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found with id: " + tagId));
        
        imageTagRepository.deleteByTag(tag);
    }

    /**
     * 모든 이미지-태그 연결 조회
     */
    @Transactional(readOnly = true)
    public List<ImageTag> getAllImageTags() {
        return imageTagRepository.findAll();
    }
}
