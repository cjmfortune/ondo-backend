package com.ondoproject.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import com.ondoproject.repository.WorkRepository;
import com.ondoproject.dto.WorkResponse;

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


    public void createWorkItem(String itemName) {
        // Logic to create a new work item
        System.out.println("Creating work item: " + itemName);
    }

    public void updateWorkItem(int itemId, String newName) {
        // Logic to update an existing work item
        System.out.println("Updating work item ID " + itemId + " to new name: " + newName);
    }

    public void deleteWorkItem(int itemId) {
        // Logic to delete a work item
        System.out.println("Deleting work item ID: " + itemId);
    }
}
