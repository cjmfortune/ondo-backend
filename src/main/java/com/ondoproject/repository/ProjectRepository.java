package com.ondoproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ondoproject.domain.project.Project;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByOrderByIndexAsc();
}
