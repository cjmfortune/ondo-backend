package com.ondoproject.repository;

import com.ondoproject.domain.news.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {
    
    // 발행된 뉴스만 조회
    List<News> findByIsPublishedTrueOrderByCreatedAtDesc();
    
    // 발행된 뉴스 페이징 조회
    Page<News> findByIsPublishedTrueOrderByCreatedAtDesc(Pageable pageable);
    
    // 제목으로 검색 (발행된 뉴스만)
    @Query("SELECT n FROM News n WHERE n.isPublished = true AND n.title LIKE %:title% ORDER BY n.createdAt DESC")
    List<News> findByTitleContainingAndIsPublishedTrue(@Param("title") String title);
    
    // 작성자로 검색 (발행된 뉴스만)
    List<News> findByAuthorContainingAndIsPublishedTrueOrderByCreatedAtDesc(String author);
    
    // 모든 뉴스 조회 (관리자용)
    List<News> findAllByOrderByCreatedAtDesc();
}
