package com.example.taskmanagementapp.services;

import com.dropbox.core.DbxException;
import com.example.taskmanagementapp.dtos.attachment.response.AttachmentResponse;
import com.example.taskmanagementapp.exceptions.ForbiddenException;
import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface AttachmentService {
    List<AttachmentResponse> uploadAttachmentForTask(Long authenticatedUserId, Long taskId,
                                                MultipartFile[] files) throws ForbiddenException,
                                                IOException, DbxException;

    List<AttachmentResponse> getAttachmentForTask(Long authenticatedUserId, Long taskId)
            throws ForbiddenException;

    void deleteAttachmentFromTask(Long authenticatedUserId, Long taskId, Long attachmentId)
            throws DbxException, ForbiddenException;
}
