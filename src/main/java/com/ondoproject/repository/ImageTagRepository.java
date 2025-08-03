package com.ondoproject.repository;

import com.ondoproject.domain.project.ImageTag;
import com.ondoproject.domain.project.Images_Info;
import com.ondoproject.domain.tag.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ImageTagRepository extends JpaRepository<ImageTag, Long> {
    
    // 특정 이미지의 모든 태그 조회
    List<ImageTag> findByImagesInfo(Images_Info imagesInfo);
    
    // 특정 태그의 모든 이미지 조회
    List<ImageTag> findByTag(Tag tag);
    
    // 특정 이미지 ID로 태그들 조회
    @Query("SELECT it FROM ImageTag it WHERE it.imagesInfo.id = :imageId")
    List<ImageTag> findByImageId(@Param("imageId") Long imageId);
    
    // 여러 이미지 ID들에 대한 태그들을 한 번에 조회 (N+1 문제 해결)
    @Query("SELECT it FROM ImageTag it " +
           "JOIN FETCH it.tag " +
           "WHERE it.imagesInfo.id IN :imageIds")
    List<ImageTag> findByImageIds(@Param("imageIds") List<Long> imageIds);
    
    // 특정 태그 ID로 이미지들 조회
    @Query("SELECT it FROM ImageTag it WHERE it.tag.id = :tagId")
    List<ImageTag> findByTagId(@Param("tagId") Long tagId);
    
    // 특정 이미지와 태그의 연결 관계 존재 여부 확인
    boolean existsByImagesInfoAndTag(Images_Info imagesInfo, Tag tag);
    
    // 특정 이미지와 태그의 연결 관계 삭제
    void deleteByImagesInfoAndTag(Images_Info imagesInfo, Tag tag);
    
    // 특정 이미지의 모든 태그 연결 삭제
    void deleteByImagesInfo(Images_Info imagesInfo);
    
    // 특정 이미지 ID로 모든 태그 연결 삭제 (수정된 버전)
    @Modifying
    @Transactional
    @Query("DELETE FROM ImageTag it WHERE it.imagesInfo.id = :imageId")
    int deleteByImagesInfoId(@Param("imageId") Long imageId);
    
    // 특정 태그의 모든 이미지 연결 삭제
    void deleteByTag(Tag tag);
}
