package com.example.taskmanagementapp.repositories.project;

import com.example.taskmanagementapp.entities.Project;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    //TODO HOW???
    List<Project> findByEmployeeId(Long employeeId);

    List<Project> findByOwnerId(Long ownerId);
}
