package com.example.taskmanagementapp.repositories.label;

import com.example.taskmanagementapp.entities.Label;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabelRepository extends JpaRepository<Label, Long> {
}
