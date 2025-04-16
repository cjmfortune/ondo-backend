package com.ondoproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ondoproject.domain.work.Work;
public interface WorkRepository extends JpaRepository<Work, Long> {
}
