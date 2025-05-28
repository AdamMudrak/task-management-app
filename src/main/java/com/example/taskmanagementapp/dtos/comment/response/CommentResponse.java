package com.example.taskmanagementapp.dtos.comment.response;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        Long taskId,
        Long userId,
        String text,
        LocalDateTime timestamp) {
}
