package com.example.taskmanagementapp.repository;

import com.example.taskmanagementapp.entity.Comment;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM Comment c WHERE c.task.id = :taskId")
    void deleteAllByTaskId(Long taskId);

    Page<Comment> findAllByTaskId(Long taskId, Pageable pageable);

    Optional<Comment> findByIdAndUserId(Long id, Long userId);
}
