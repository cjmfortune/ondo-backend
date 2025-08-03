package com.ondoproject.repository;

import com.ondoproject.domain.tag.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    // 태그명으로 존재 여부 확인
    boolean existsByTagName(String tagName);
    
    // 특정 ID를 제외하고 태그명으로 존재 여부 확인 (수정 시 사용)
    boolean existsByTagNameAndIdNot(String tagName, Long id);
    
    // 태그명으로 태그 찾기
    Tag findByTagName(String tagName);
}
