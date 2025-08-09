package com.example.taskmanagementapp.dto.attachment.response;

import java.time.LocalDateTime;

public record AttachmentResponse(
        Long id,
        Long taskId,
        String fileId,
        String fileName,
        LocalDateTime uploadDate){}
