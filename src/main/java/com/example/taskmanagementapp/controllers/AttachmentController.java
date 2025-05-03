package com.example.taskmanagementapp.controllers;

import static com.example.taskmanagementapp.constants.Constants.ROLE_EMPLOYEE;
import static com.example.taskmanagementapp.constants.Constants.ROLE_MANAGER;
import static com.example.taskmanagementapp.constants.Constants.ROLE_SUPERVISOR;

import com.dropbox.core.DbxException;
import com.example.taskmanagementapp.dtos.attachment.response.AttachmentDto;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.forbidden.ForbiddenException;
import com.example.taskmanagementapp.services.AttachmentService;
import jakarta.validation.constraints.Positive;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


//TODO добавить тут константы
@RestController
@RequestMapping("/attachments")
@RequiredArgsConstructor
@Validated
public class AttachmentController {
    private final AttachmentService attachmentService;

    @PreAuthorize(ROLE_EMPLOYEE + " or "
            + ROLE_MANAGER + " or "
            + ROLE_SUPERVISOR)
    @PostMapping(path = "/{taskId}",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<AttachmentDto> addAttachment(@AuthenticationPrincipal User user,
                                       MultipartFile[] attachmentFile,
                                       @PathVariable @Positive Long taskId)
            throws ForbiddenException, IOException, DbxException {
        return attachmentService.uploadAttachmentForTask(user, taskId, attachmentFile);
    }

    @PreAuthorize(ROLE_EMPLOYEE + " or "
            + ROLE_MANAGER + " or "
            + ROLE_SUPERVISOR)
    @GetMapping("/{taskId}")
    public List<AttachmentDto> getAttachment(@AuthenticationPrincipal User user,
                                                        @PathVariable @Positive Long taskId)
            throws ForbiddenException, IOException, DbxException {
        return attachmentService.getAttachmentForTask(user, taskId);
    }

    @DeleteMapping("/{taskId}/{attachmentId}")
    public void deleteAttachment(@AuthenticationPrincipal User user,
                                 @PathVariable @Positive Long taskId,
                                 @PathVariable @Positive Long attachmentId)
            throws DbxException, ForbiddenException {
        attachmentService.deleteAttachmentFromTask(user, taskId, attachmentId);
    }
}
