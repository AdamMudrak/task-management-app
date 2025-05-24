package com.example.taskmanagementapp.services.impl;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.sharing.CreateSharedLinkWithSettingsErrorException;
import com.dropbox.core.v2.sharing.ListSharedLinksResult;
import com.example.taskmanagementapp.dtos.attachment.response.AttachmentDto;
import com.example.taskmanagementapp.entities.Attachment;
import com.example.taskmanagementapp.entities.Task;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.EntityNotFoundException;
import com.example.taskmanagementapp.exceptions.ForbiddenException;
import com.example.taskmanagementapp.mappers.AttachmentMapper;
import com.example.taskmanagementapp.repositories.AttachmentRepository;
import com.example.taskmanagementapp.repositories.ProjectRepository;
import com.example.taskmanagementapp.repositories.TaskRepository;
import com.example.taskmanagementapp.services.AttachmentService;
import com.example.taskmanagementapp.services.utils.TransliterationUtil;
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
    private final ProjectRepository projectRepository;
    private final AttachmentMapper attachmentMapper;
    private final TransliterationUtil transliterationService;

    @Override
    public List<AttachmentDto> uploadAttachmentForTask(User authenticatedUser,
                                          Long taskId,
                                          MultipartFile[] uploadFiles) throws ForbiddenException,
                                                                                    IOException,
                                                                                    DbxException {
        Task task = taskRepository.findByIdNotDeleted(taskId).orElseThrow(
                () -> new EntityNotFoundException("Active task with id "
                        + taskId + " not found"));
        Long thisTaskProjectId = task.getProject().getId();
        Long thisUserId = authenticatedUser.getId();

        if (projectRepository.isUserOwner(thisTaskProjectId, thisUserId)
                || projectRepository.isUserEmployee(thisTaskProjectId, thisUserId)
                || projectRepository.isUserManager(thisTaskProjectId, thisUserId)) {
            List<Attachment> attachments = uploadFilesToDropbox(uploadFiles, task);
            return attachmentMapper.toAttachmentDtoList(attachmentRepository.saveAll(attachments));
        } else {
            throw new ForbiddenException("You have no permission to add attachment to task "
                    + task.getId() + " since you are not in project " + task.getProject().getId());
        }
    }

    @Override
    public List<AttachmentDto> getAttachmentForTask(User authenticatedUser, Long taskId)
            throws ForbiddenException {
        Task task = taskRepository.findByIdNotDeleted(taskId).orElseThrow(
                () -> new EntityNotFoundException("Active task with id "
                        + taskId + " not found"));
        Long thisTaskProjectId = task.getProject().getId();
        Long thisUserId = authenticatedUser.getId();

        if (projectRepository.isUserOwner(thisTaskProjectId, thisUserId)
                || projectRepository.isUserEmployee(thisTaskProjectId, thisUserId)
                || projectRepository.isUserManager(thisTaskProjectId, thisUserId)) {
            return attachmentMapper.toAttachmentDtoList(
                    attachmentRepository.findAllByTaskId(taskId));
        } else {
            throw new ForbiddenException("You have no permission to get attachments for task "
                    + task.getId() + " since you are not in project " + task.getProject().getId());
        }
    }

    @Override
    public void deleteAttachmentFromTask(User authenticatedUser, Long taskId, Long attachmentId)
            throws DbxException, ForbiddenException {
        Task task = taskRepository.findByIdNotDeleted(taskId).orElseThrow(
                () -> new EntityNotFoundException("Active task with id "
                        + taskId + " not found"));
        Attachment attachment = attachmentRepository.findById(attachmentId).orElseThrow(
                () -> new EntityNotFoundException("Attachment with id "
                + attachmentId + " not found"));

        Long thisTaskProjectId = task.getProject().getId();
        Long thisUserId = authenticatedUser.getId();

        if (projectRepository.isUserOwner(thisTaskProjectId, thisUserId)
                || projectRepository.isUserEmployee(thisTaskProjectId, thisUserId)
                || projectRepository.isUserManager(thisTaskProjectId, thisUserId)) {
            client.files().deleteV2("/task" + task.getId()
                    + "/" + attachment.getFileName());
            attachmentRepository.deleteById(attachmentId);
        } else {
            throw new ForbiddenException("You have no permission to delete attachment from task "
                    + task.getId() + " since you are not in project " + task.getProject().getId());
        }
    }

    private List<Attachment> uploadFilesToDropbox(MultipartFile[] uploadFiles, Task task)
                                                        throws IOException, DbxException {
        List<Attachment> attachments = new ArrayList<>();
        for (MultipartFile uploadFile : uploadFiles) {
            String fileName = transliterationService
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
}
