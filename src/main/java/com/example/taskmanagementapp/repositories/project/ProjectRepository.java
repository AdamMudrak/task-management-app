package com.example.taskmanagementapp.repositories.project;

import com.example.taskmanagementapp.entities.Project;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query(value = "SELECT * FROM project p "
            + "WHERE EXISTS ("
            + "SELECT 1 FROM project_employees pe "
            + "WHERE pe.project_id = p.id "
            + "AND pe.employee_id = :employeeId) "
            + "AND p.is_deleted = 0", nativeQuery = true)
    List<Project> findByEmployeeId(Long employeeId);

    @Query("SELECT p FROM Project p "
            + "WHERE p.owner.id = :ownerId "
            + " AND p.isDeleted = false ")
    List<Project> findByOwnerIdAndIsDeletedFalse(Long ownerId);

    @Query("SELECT p FROM Project p "
            + "WHERE p.isDeleted = false")
    List<Project> findAllNotDeleted();

    @Query("SELECT p FROM Project p "
            + "WHERE p.isDeleted = true")
    List<Project> findAllDeleted();
}
