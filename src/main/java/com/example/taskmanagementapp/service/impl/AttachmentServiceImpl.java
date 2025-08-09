package com.example.taskmanagementapp.service.impl;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.sharing.CreateSharedLinkWithSettingsErrorException;
import com.dropbox.core.v2.sharing.ListSharedLinksResult;
import com.example.taskmanagementapp.dto.attachment.response.AttachmentResponse;
import com.example.taskmanagementapp.entity.Attachment;
import com.example.taskmanagementapp.entity.Task;
import com.example.taskmanagementapp.exception.EntityNotFoundException;
import com.example.taskmanagementapp.exception.ForbiddenException;
import com.example.taskmanagementapp.mapper.AttachmentMapper;
import com.example.taskmanagementapp.repository.AttachmentRepository;
import com.example.taskmanagementapp.repository.TaskRepository;
import com.example.taskmanagementapp.service.AttachmentService;
import com.example.taskmanagementapp.service.utils.ProjectAuthorityUtil;
import com.example.taskmanagementapp.service.utils.TransliterationUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class AttachmentServiceImpl implements AttachmentService {
    private final DbxClientV2 client;
    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final AttachmentMapper attachmentMapper;
    private final ProjectAuthorityUtil projectAuthorityUtil;

    @Override
    public List<AttachmentResponse> uploadAttachmentForTask(Long authenticatedUserId, Long taskId,
                                            MultipartFile[] uploadFiles) throws ForbiddenException,
                                            IOException, DbxException {
        Task task = getTaskById(taskId);
        Long thisTaskProjectId = task.getProject().getId();

        if (projectAuthorityUtil.hasAnyAuthority(thisTaskProjectId, authenticatedUserId)) {
            List<Attachment> attachments = uploadFilesToDropbox(uploadFiles, task);
            return attachmentMapper.toAttachmentDtoList(attachmentRepository.saveAll(attachments));
        } else {
            throw new ForbiddenException("You have no permission to add attachment to task "
                    + taskId + " since you are not in project " + thisTaskProjectId);
        }
    }

    @Override
    public List<AttachmentResponse> getAttachmentForTask(Long authenticatedUserId, Long taskId)
            throws ForbiddenException {
        Task task = getTaskById(taskId);
        Long thisTaskProjectId = task.getProject().getId();

        if (projectAuthorityUtil.hasAnyAuthority(thisTaskProjectId, authenticatedUserId)) {
            return attachmentMapper.toAttachmentDtoList(
                    attachmentRepository.findAllByTaskId(taskId));
        } else {
            throw new ForbiddenException("You have no permission to get attachments for task "
                    + taskId + " since you are not in project " + thisTaskProjectId);
        }
    }

    @Override
    public void deleteAttachmentFromTask(Long authenticatedUserId, Long taskId, Long attachmentId)
            throws DbxException, ForbiddenException {
        Task task = getTaskById(taskId);
        Attachment attachment = attachmentRepository.findById(attachmentId).orElseThrow(
                () -> new EntityNotFoundException("Attachment with id "
                + attachmentId + " not found"));

        Long thisTaskProjectId = task.getProject().getId();

        if (projectAuthorityUtil.hasAnyAuthority(thisTaskProjectId, authenticatedUserId)) {
            client.files().deleteV2("/task" + taskId
                    + "/" + attachment.getFileName());
            attachmentRepository.deleteById(attachmentId);
        } else {
            throw new ForbiddenException("You have no permission to delete attachment from task "
                    + taskId + " since you are not in project " + thisTaskProjectId);
        }
    }

    private List<Attachment> uploadFilesToDropbox(MultipartFile[] uploadFiles, Task task)
                                                        throws IOException, DbxException {
        List<Attachment> attachments = new ArrayList<>();
        for (MultipartFile uploadFile : uploadFiles) {
            String fileName = TransliterationUtil
                    .transliterate(uploadFile.getOriginalFilename());
            try (InputStream stream = uploadFile.getInputStream()) {
                client.files().uploadBuilder("/task" + task.getId() + "/"
                        + fileName).uploadAndFinish(stream);
            }
            String sharedLink = makeDropboxLinkToRawFile(getOrCreateSharedLink(client,
                    "/task" + task.getId() + "/" + fileName));
            Attachment thisAttachment = attachmentMapper.toAttachment(
                    task, sharedLink, fileName);
            attachments.add(thisAttachment);
        }
        return attachments;
    }

    private String getOrCreateSharedLink(DbxClientV2 client, String dropboxPath)
            throws DbxException {
        try {
            return client.sharing()
                    .createSharedLinkWithSettings(dropboxPath)
                    .getUrl();
        } catch (CreateSharedLinkWithSettingsErrorException e) {
            if (e.errorValue.isSharedLinkAlreadyExists()) {
                ListSharedLinksResult existingLinks = client.sharing().listSharedLinksBuilder()
                        .withPath(dropboxPath)
                        .withDirectOnly(true)
                        .start();
                if (!existingLinks.getLinks().isEmpty()) {
                    return existingLinks.getLinks().getFirst().getUrl();
                }
            }
            throw e;
        }
    }

    private String makeDropboxLinkToRawFile(String link) {
        return link.replace("dl=0", "raw=1");
    }

    private Task getTaskById(Long taskId) {
        return taskRepository.findByIdNotDeleted(taskId).orElseThrow(
                () -> new EntityNotFoundException("Active task with id "
                        + taskId + " not found"));
    }
}
