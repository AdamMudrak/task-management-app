package com.example.taskmanagementapp.mappers;

import com.example.taskmanagementapp.config.MapperConfig;
import com.example.taskmanagementapp.dtos.attachment.response.AttachmentDto;
import com.example.taskmanagementapp.entities.Attachment;
import com.example.taskmanagementapp.entities.Task;
import java.time.LocalDateTime;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface AttachmentMapper {
    default Attachment toAttachment(Task task, String fileId, String fileName) {
        Attachment attachment = new Attachment();
        attachment.setTask(task);
        attachment.setFileId(fileId);
        attachment.setFileName(fileName);
        attachment.setUploadDate(LocalDateTime.now());
        return attachment;
    }

    @Mapping(target = "taskId", source = "task.id")
    AttachmentDto toAttachmentDto(Attachment attachment);

    List<AttachmentDto> toAttachmentDtoList(List<Attachment> attachments);
}
