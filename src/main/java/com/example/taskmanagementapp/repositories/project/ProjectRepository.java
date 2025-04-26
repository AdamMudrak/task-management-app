package com.example.taskmanagementapp.repositories.project;

import com.example.taskmanagementapp.entities.Project;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query(value = "SELECT * FROM project p "
            + "WHERE EXISTS ("
            + "SELECT FROM project_employees pe "
            + "WHERE pe.project_id = p.id "
            + "AND pe.employee_id = :employeeId", nativeQuery = true)
    List<Project> findByEmployeeId(Long employeeId);

    List<Project> findByOwnerId(Long ownerId);
}
