package com.example.taskmanagementapp.repositories.comment;

import com.example.taskmanagementapp.entities.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
