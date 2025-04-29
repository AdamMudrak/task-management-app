package com.example.taskmanagementapp.repositories.project;

import com.example.taskmanagementapp.entities.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query("SELECT p FROM Project p "
            + "WHERE EXISTS ("
            + "SELECT 1 FROM p.employees pe "
            + "WHERE pe.id = :employeeId) "
            + "AND p.isDeleted = false")
    Page<Project> findAllByEmployeeId(Long employeeId, Pageable pageable);

    @Query("SELECT p FROM Project p "
            + "WHERE p.owner.id = :ownerId "
            + " AND p.isDeleted = false ")
    Page<Project> findAllByOwnerId(Long ownerId, Pageable pageable);

    @Query("SELECT p FROM Project p "
            + "WHERE p.isDeleted = false")
    Page<Project> findAllNonDeleted(Pageable pageable);

    @Query("SELECT p FROM Project p "
            + "WHERE p.isDeleted = true")
    Page<Project> findAllDeleted(Pageable pageable);
}
