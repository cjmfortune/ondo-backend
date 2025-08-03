package com.ondoproject.service.about;

import com.ondoproject.domain.about.About;
import com.ondoproject.dto.about.AboutResponse;
import com.ondoproject.repository.AboutRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AboutService {
    private AboutRepository aboutRepository;

    public List<AboutResponse> getAll() {
        return aboutRepository.findAll().stream()
                .map(about -> AboutResponse.builder()
                        .id(about.getId())
                        .description(about.getDescription())
                        .description2(about.getDescription2())
                        .build())
                .toList();
    }
}
