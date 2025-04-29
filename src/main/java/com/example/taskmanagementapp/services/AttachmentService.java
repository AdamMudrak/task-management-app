package com.example.taskmanagementapp.services;

import com.example.taskmanagementapp.dtos.attachment.request.UploadAttachmentDto;
import com.example.taskmanagementapp.dtos.attachment.response.AttachmentDto;
import com.example.taskmanagementapp.entities.User;
import java.util.List;

public interface AttachmentService {
    void uploadAttachmentForTask(User authenticatedUser, UploadAttachmentDto uploadAttachmentDto);

    List<AttachmentDto> getAttachmentsForTask(Long taskId);
}
