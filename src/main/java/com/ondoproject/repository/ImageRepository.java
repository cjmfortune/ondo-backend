package com.ondoproject.repository;

import com.ondoproject.domain.project.Images_Info;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Images_Info, Long> {
    public Images_Info findByProjectId(Long id);
    public List<Images_Info> findAllByIsShowTrueOrderByIndexAsc();
    public List<Images_Info> findAllByOrderByIndexAsc(); // 모든 이미지를 index 순으로 정렬
    
    // 태그 정보까지 함께 fetch join으로 가져오는 쿼리 (N+1 문제 해결)
    @Query("SELECT DISTINCT i FROM Images_Info i " +
           "LEFT JOIN FETCH i.project " +
           "WHERE i.isShow = true " +
           "ORDER BY i.index ASC")
    List<Images_Info> findAllByIsShowTrueWithProjectOrderByIndexAsc();
    
    // 모든 이미지를 프로젝트 정보와 함께 조회 (isShow 값에 상관없이)
    @Query("SELECT DISTINCT i FROM Images_Info i " +
           "LEFT JOIN FETCH i.project " +
           "ORDER BY i.index ASC")
    List<Images_Info> findAllWithProjectOrderByIndexAsc();
}
