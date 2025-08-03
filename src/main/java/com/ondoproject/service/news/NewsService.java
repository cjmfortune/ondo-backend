package com.ondoproject.service.news;

import com.ondoproject.domain.news.News;
import com.ondoproject.dto.news.*;
import com.ondoproject.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {
    
    private final NewsRepository newsRepository;

    // 모든 뉴스 조회 (발행된 것만)
    public List<NewsListResponse> getAllNews() {
        return newsRepository.findByIsPublishedTrueOrderByCreatedAtDesc().stream()
                .map(this::convertToListResponse)
                .toList();
    }
    
    // 페이징된 뉴스 조회
    public Page<NewsListResponse> getNewsWithPaging(Pageable pageable) {
        return newsRepository.findByIsPublishedTrueOrderByCreatedAtDesc(pageable)
                .map(this::convertToListResponse);
    }
    
    // 특정 뉴스 조회
    public NewsResponse getNewsById(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("뉴스를 찾을 수 없습니다. ID: " + id));
        
        return convertToResponse(news);
    }
    
    // 뉴스 생성
    @Transactional
    public NewsResponse createNews(NewsCreateRequest request) {
        News news = News.builder()
                .title(request.getTitle())
                .contents(request.getContents())
                .imageURL(request.getImageURL())
                .fileName(request.getFileName())
                .fileType(request.getFileType())
                .isPublished(request.getIsPublished())
                .author(request.getAuthor())
                .summary(request.getSummary())
                .build();
        
        News savedNews = newsRepository.save(news);
        return convertToResponse(savedNews);
    }
    
    // 뉴스 수정
    @Transactional
    public NewsResponse updateNews(Long id, NewsUpdateRequest request) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("뉴스를 찾을 수 없습니다. ID: " + id));
        
        // 필드별로 null 체크 후 업데이트
        if (request.getTitle() != null) {
            news.setTitle(request.getTitle());
        }
        if (request.getContents() != null) {
            news.setContents(request.getContents());
        }
        if (request.getImageURL() != null) {
            news.setImageURL(request.getImageURL());
        }
        if (request.getFileName() != null) {
            news.setFileName(request.getFileName());
        }
        if (request.getFileType() != null) {
            news.setFileType(request.getFileType());
        }
        if (request.getIsPublished() != null) {
            news.setIsPublished(request.getIsPublished());
        }
        if (request.getAuthor() != null) {
            news.setAuthor(request.getAuthor());
        }
        if (request.getSummary() != null) {
            news.setSummary(request.getSummary());
        }
        
        News updatedNews = newsRepository.save(news);
        return convertToResponse(updatedNews);
    }
    
    // 뉴스 삭제
    @Transactional
    public void deleteNews(Long id) {
        if (!newsRepository.existsById(id)) {
            throw new RuntimeException("뉴스를 찾을 수 없습니다. ID: " + id);
        }
        newsRepository.deleteById(id);
    }
    
    // 제목으로 검색
    public List<NewsListResponse> searchByTitle(String title) {
        return newsRepository.findByTitleContainingAndIsPublishedTrue(title).stream()
                .map(this::convertToListResponse)
                .toList();
    }
    
    // 작성자로 검색
    public List<NewsListResponse> searchByAuthor(String author) {
        return newsRepository.findByAuthorContainingAndIsPublishedTrueOrderByCreatedAtDesc(author).stream()
                .map(this::convertToListResponse)
                .toList();
    }
    
    // 관리자용 - 모든 뉴스 조회 (미발행 포함)
    public List<NewsListResponse> getAllNewsForAdmin() {
        return newsRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::convertToListResponse)
                .toList();
    }
    
    // Entity -> Response 변환
    private NewsResponse convertToResponse(News news) {
        return NewsResponse.builder()
                .id(news.getId())
                .title(news.getTitle())
                .contents(news.getContents())
                .imageURL(news.getImageURL())
                .fileName(news.getFileName())
                .fileType(news.getFileType())
                .createdAt(news.getCreatedAt())
                .updatedAt(news.getUpdatedAt())
                .isPublished(news.getIsPublished())
                .author(news.getAuthor())
                .summary(news.getSummary())
                .build();
    }
    
    // Entity -> ListResponse 변환
    private NewsListResponse convertToListResponse(News news) {
        return NewsListResponse.builder()
                .id(news.getId())
                .title(news.getTitle())
                .summary(news.getSummary())
                .imageURL(news.getImageURL())
                .createdAt(news.getCreatedAt())
                .updatedAt(news.getUpdatedAt())
                .isPublished(news.getIsPublished())
                .author(news.getAuthor())
                .build();
    }
}
