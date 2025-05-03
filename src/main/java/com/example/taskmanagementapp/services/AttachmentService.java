package com.example.taskmanagementapp.services;

import com.dropbox.core.DbxException;
import com.example.taskmanagementapp.dtos.attachment.response.AttachmentDto;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.forbidden.ForbiddenException;
import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface AttachmentService {
    List<AttachmentDto> uploadAttachmentForTask(User authenticatedUser,
                                   Long taskId,
                                   MultipartFile[] files) throws ForbiddenException,
                                                        IOException, DbxException;

    List<AttachmentDto> getAttachmentForTask(User authenticatedUser, Long taskId)
            throws IOException, DbxException, ForbiddenException;

    void deleteAttachmentFromTask(User authenticatedUser, Long taskId, Long attachmentId)
            throws DbxException, ForbiddenException;
}
