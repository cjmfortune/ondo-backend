package com.ondoproject.controller;

import com.ondoproject.dto.news.NewsResponse;
import com.ondoproject.repository.NewsRepository;
import com.ondoproject.service.news.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Tag(name = "뉴스", description = "뉴스 관련 API")
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {
    private final NewsService newsService;
    //Read
    @RequestMapping(value = "/read", method = RequestMethod.GET)
    public List<NewsResponse> getAllNews() {
        return newsService.getAllNews();
    }
}
