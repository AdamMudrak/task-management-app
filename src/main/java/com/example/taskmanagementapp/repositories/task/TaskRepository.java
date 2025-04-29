package com.example.taskmanagementapp.repositories.task;

import com.example.taskmanagementapp.entities.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("SELECT t FROM Task t "
            + "WHERE t.project.id = :projectId "
            + " AND t.isDeleted = false")
    Page<Task> findAllByProjectIdNonDeleted(Long projectId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.isDeleted = TRUE "
            + "WHERE t.project.id = :projectId")
    void deleteAllByProjectId(Long projectId);
}
