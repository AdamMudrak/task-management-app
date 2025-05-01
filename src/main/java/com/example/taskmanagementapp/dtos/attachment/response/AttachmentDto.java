package com.example.taskmanagementapp.dtos.attachment.response;

import java.time.LocalDateTime;

public record AttachmentDto(
        Long id,
        Long taskId,
        String fileId,
        String fileName,
        LocalDateTime uploadDate) {}
