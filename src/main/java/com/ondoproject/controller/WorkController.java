package com.ondoproject.controller;

import com.ondoproject.dto.WorkResponse;
import com.ondoproject.service.WorkService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/works")
@RequiredArgsConstructor
public class WorkController {

    private final WorkService workService;

    @Operation(summary = "전체 작품 조회",
            description = "전체 작품을 조회합니다.")
    @GetMapping
    public List<WorkResponse> getAll() {
        return workService.getAllWorks();
    }

}