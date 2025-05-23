package com.example.taskmanagementapp.controllers;

import static com.example.taskmanagementapp.constants.Constants.CODE_200;
import static com.example.taskmanagementapp.constants.Constants.CODE_201;
import static com.example.taskmanagementapp.constants.Constants.CODE_204;
import static com.example.taskmanagementapp.constants.controllers.AttachmentControllerConstants.ATTACHMENTS_API_DESCRIPTION;
import static com.example.taskmanagementapp.constants.controllers.AttachmentControllerConstants.ATTACHMENTS_API_NAME;
import static com.example.taskmanagementapp.constants.controllers.AttachmentControllerConstants.DELETE_ATTACHMENT_SUMMARY;
import static com.example.taskmanagementapp.constants.controllers.AttachmentControllerConstants.GET_ATTACHMENT_SUMMARY;
import static com.example.taskmanagementapp.constants.controllers.AttachmentControllerConstants.SUCCESSFULLY_DELETED_ATTACHMENTS;
import static com.example.taskmanagementapp.constants.controllers.AttachmentControllerConstants.SUCCESSFULLY_GOT_ATTACHMENTS;
import static com.example.taskmanagementapp.constants.controllers.AttachmentControllerConstants.SUCCESSFULLY_UPLOADED_ATTACHMENTS;
import static com.example.taskmanagementapp.constants.controllers.AttachmentControllerConstants.UPLOAD_ATTACHMENT_SUMMARY;

import com.dropbox.core.DbxException;
import com.example.taskmanagementapp.dtos.attachment.response.AttachmentDto;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.forbidden.ForbiddenException;
import com.example.taskmanagementapp.services.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/attachments")
@Tag(name = ATTACHMENTS_API_NAME, description = ATTACHMENTS_API_DESCRIPTION)
@RequiredArgsConstructor
@Validated
public class AttachmentController {
    private final AttachmentService attachmentService;

    @Operation(summary = UPLOAD_ATTACHMENT_SUMMARY)
    @ApiResponse(responseCode = CODE_201, description =
            SUCCESSFULLY_UPLOADED_ATTACHMENTS)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping(path = "/{taskId}",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    public List<AttachmentDto> addAttachment(@AuthenticationPrincipal User user,
                                       MultipartFile[] attachmentFile,
                                       @PathVariable @Positive Long taskId)
            throws ForbiddenException, IOException, DbxException {
        return attachmentService.uploadAttachmentForTask(user, taskId, attachmentFile);
    }

    @Operation(summary = GET_ATTACHMENT_SUMMARY)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_GOT_ATTACHMENTS)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{taskId}")
    public List<AttachmentDto> getAttachment(@AuthenticationPrincipal User user,
                                                        @PathVariable @Positive Long taskId)
            throws ForbiddenException {
        return attachmentService.getAttachmentForTask(user, taskId);
    }

    @Operation(summary = DELETE_ATTACHMENT_SUMMARY)
    @ApiResponse(responseCode = CODE_204, description =
            SUCCESSFULLY_DELETED_ATTACHMENTS)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping("/{taskId}/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAttachment(@AuthenticationPrincipal User user,
                                 @PathVariable @Positive Long taskId,
                                 @PathVariable @Positive Long attachmentId)
            throws DbxException, ForbiddenException {
        attachmentService.deleteAttachmentFromTask(user, taskId, attachmentId);
    }
}
