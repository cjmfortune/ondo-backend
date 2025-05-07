package com.ondoproject.service;

import com.ondoproject.domain.work.Work;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import com.ondoproject.repository.WorkRepository;
import com.ondoproject.dto.work.WorkResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@AllArgsConstructor
public class WorkService {
    private final WorkRepository workRepository;

    public List<WorkResponse> getAllWorks() {
        return workRepository.findAll().stream()
                .map(work -> new WorkResponse(work.getId(), work.getTitle(), work.getImageURL()))
                .toList();
    }

    public void createWorkItem(MultipartFile image, String title, String description) {
        String fileName = image.getOriginalFilename();
        String uploadPath = System.getProperty("user.dir") + "/uploads/";
        File directory = new File(uploadPath);
        if (!directory.exists()) directory.mkdirs();

        File targetFile = new File(uploadPath + fileName);
        try {
            image.transferTo(targetFile);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }

        Work work = Work.builder()
                .title(title)
                .description(description)
                .imageURL(uploadPath + fileName)
                .build();

        workRepository.save(work);
    }

    public void updateWorkItem(Long itemId ,MultipartFile image, String title,  String description) {
        // Logic to update an existing work item
        var workById = workRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Work item not found"));
        title = title != null ? title : workById.getTitle();
        description = description != null ? description : workById.getDescription();
        var imageURL = workById.getImageURL();
        if (image != null && !image.isEmpty()) {
            String fileName = image.getOriginalFilename();
            String uploadPath = System.getProperty("user.dir") + "/uploads/";
            File directory = new File(uploadPath);
            if (!directory.exists()) directory.mkdirs();

            File targetFile = new File(uploadPath + fileName);
            try {
                image.transferTo(targetFile);
                imageURL = uploadPath + fileName;
            } catch (IOException e) {
                throw new RuntimeException("이미지 저장 실패", e);
            }
        }
        Work work = Work.builder()
                    .title(title)
                    .description(description)
                    .imageURL(imageURL)
                    .build();
        workRepository.save(work);
    }

    public void deleteWorkItem(Long itemId) {
        // Logic to delete a work item
        var workById = workRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Work item not found"));
        workRepository.delete(workById);
    }
}
