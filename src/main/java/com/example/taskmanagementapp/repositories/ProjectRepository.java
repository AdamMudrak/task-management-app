package com.example.taskmanagementapp.repositories;

import com.example.taskmanagementapp.entities.Project;
import java.util.Optional;
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
            + "WHERE p.owner.id = :ownerId "
            + " AND p.isDeleted = true ")
    Page<Project> findAllByOwnerIdDeleted(Long ownerId, Pageable pageable);

    @Query("SELECT p FROM Project p "
            + "WHERE p.id = :id "
            + " AND p.isDeleted = false")
    Optional<Project> findByIdNotDeleted(Long id);

    @Query("SELECT COUNT(p) > 0 FROM Project p "
            + "JOIN p.managers m "
            + "WHERE p.id = :projectId AND m.id = :userId AND p.isDeleted = false")
    boolean isUserManager(Long projectId, Long userId);

    @Query("SELECT COUNT(p) > 0 FROM Project p "
            + "JOIN p.employees e "
            + "WHERE p.id = :projectId AND e.id = :userId AND p.isDeleted = false")
    boolean isUserEmployee(Long projectId, Long userId);

    @Query("SELECT COUNT(p) > 0 FROM Project p "
            + "WHERE p.id = :projectId AND p.owner.id = :userId AND p.isDeleted = false")
    boolean isUserOwner(Long projectId, Long userId);

    @Query("SELECT COUNT(p) > 0 FROM Project p WHERE p.id = :id AND p.isDeleted = false")
    boolean existsByIdNotDeleted(Long id);
}
