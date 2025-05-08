package com.example.taskmanagementapp.repositories.label;

import com.example.taskmanagementapp.entities.Label;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabelRepository extends JpaRepository<Label, Long> {
    Optional<Label> findByIdAndUserId(Long id, Long userId);

    Page<Label> findAllByUserId(Long userId, Pageable pageable);

    boolean existsByIdAndUserId(Long id, Long userId);
}
