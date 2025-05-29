package com.example.taskmanagementapp.mappers;

import com.example.taskmanagementapp.config.MapperConfig;
import com.example.taskmanagementapp.dtos.attachment.response.AttachmentResponse;
import com.example.taskmanagementapp.entities.Attachment;
import com.example.taskmanagementapp.entities.Task;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface AttachmentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", source = "task")
    @Mapping(target = "fileId", source = "fileId")
    @Mapping(target = "fileName", source = "fileName")
    @Mapping(target = "uploadDate", expression = "java(java.time.LocalDateTime.now())")
    Attachment toAttachment(Task task, String fileId, String fileName);

    @Mapping(target = "taskId", source = "task.id")
    AttachmentResponse toAttachmentDto(Attachment attachment);

    List<AttachmentResponse> toAttachmentDtoList(List<Attachment> attachments);
}
