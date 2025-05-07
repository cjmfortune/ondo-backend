package com.ondoproject.service.news;

import com.ondoproject.dto.news.NewsResponse;
import com.ondoproject.repository.NewsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class NewsService {
    private final NewsRepository newsRepository;

    public List<NewsResponse> getAllNews() {
        return newsRepository.findAll().stream()
                .map(news -> NewsResponse.builder()
                        .id(news.getId())
                        .title(news.getTitle())
                        .content(news.getContents())
                        .imageURL(news.getImageURL())
                        .createDateTime(news.getCreateDateTime())
                        .updateDateTime(news.getUpdateDateTime())
                        .build())
                .toList();
    }
}
