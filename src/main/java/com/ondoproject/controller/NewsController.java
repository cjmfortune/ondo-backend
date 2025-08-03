package com.ondoproject.controller;

import com.ondoproject.dto.news.*;
import com.ondoproject.service.news.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "뉴스", description = "뉴스 관련 API")
@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
public class NewsController {
    
    private final NewsService newsService;

    @Operation(summary = "모든 뉴스 조회", description = "발행된 모든 뉴스를 최신순으로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    public ResponseEntity<List<NewsListResponse>> getAllNews() {
        List<NewsListResponse> newsList = newsService.getAllNews();
        return ResponseEntity.ok(newsList);
    }

    @Operation(summary = "뉴스 페이징 조회", description = "발행된 뉴스를 페이징하여 조회합니다.")
    @GetMapping("/paged")
    public ResponseEntity<Page<NewsListResponse>> getNewsWithPaging(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<NewsListResponse> newsPage = newsService.getNewsWithPaging(pageable);
        return ResponseEntity.ok(newsPage);
    }

    @Operation(summary = "특정 뉴스 조회", description = "ID로 특정 뉴스의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "뉴스를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{id}")
    public ResponseEntity<NewsResponse> getNewsById(
            @Parameter(description = "뉴스 ID", required = true)
            @PathVariable Long id) {
        try {
            NewsResponse news = newsService.getNewsById(id);
            return ResponseEntity.ok(news);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "뉴스 생성", description = "새로운 뉴스를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping
    public ResponseEntity<NewsResponse> createNews(
            @Parameter(description = "뉴스 생성 요청", required = true)
            @Valid @RequestBody NewsCreateRequest request) {
        try {
            NewsResponse createdNews = newsService.createNews(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdNews);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "뉴스 수정", description = "기존 뉴스를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "뉴스를 찾을 수 없음"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PutMapping("/{id}")
    public ResponseEntity<NewsResponse> updateNews(
            @Parameter(description = "뉴스 ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "뉴스 수정 요청", required = true)
            @Valid @RequestBody NewsUpdateRequest request) {
        try {
            NewsResponse updatedNews = newsService.updateNews(id, request);
            return ResponseEntity.ok(updatedNews);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "뉴스 삭제", description = "특정 뉴스를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "뉴스를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNews(
            @Parameter(description = "뉴스 ID t", required = true)
            @PathVariable Long id) {
        try {
            newsService.deleteNews(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "제목으로 뉴스 검색", description = "제목에 특정 키워드가 포함된 뉴스를 검색합니다.")
    @GetMapping("/search/title")
    public ResponseEntity<List<NewsListResponse>> searchByTitle(
            @Parameter(description = "검색할 제목 키워드", required = true)
            @RequestParam String title) {
        List<NewsListResponse> newsList = newsService.searchByTitle(title);
        return ResponseEntity.ok(newsList);
    }

    @Operation(summary = "작성자로 뉴스 검색 t", description = "특정 작성자의 뉴스를 검색합니다.")
    @GetMapping("/search/author")
    public ResponseEntity<List<NewsListResponse>> searchByAuthor(
            @Parameter(description = "검색할 작성자명", required = true)
            @RequestParam String author) {
        List<NewsListResponse> newsList = newsService.searchByAuthor(author);
        return ResponseEntity.ok(newsList);
    }

    @Operation(summary = "관리자용 모든 뉴스 조회", description = "미발행 뉴스를 포함한 모든 뉴스를 조회합니다.")
    @GetMapping("/admin/all")
    public ResponseEntity<List<NewsListResponse>> getAllNewsForAdmin() {
        List<NewsListResponse> newsList = newsService.getAllNewsForAdmin();
        return ResponseEntity.ok(newsList);
    }
}
