package com.ondoproject.service.author;

import com.ondoproject.dto.author.AuthorResponse;
import com.ondoproject.repository.AuthorRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AuthorService {

    private AuthorRepository authorRepository;

    public List<AuthorResponse> getAllMembers() {
        return authorRepository.findAll().stream()
                .map(member -> AuthorResponse.builder()
                        .id(member.getId())
                        .title(member.getTitle())
                        .name(member.getName())
                        .description(member.getDescription())
                        .build())
                .toList();
    }
}
