package com.example.taskmanagementapp.mappers;

import com.example.taskmanagementapp.config.MapperConfig;
import com.example.taskmanagementapp.dtos.comment.request.CommentRequest;
import com.example.taskmanagementapp.dtos.comment.response.CommentResponse;
import com.example.taskmanagementapp.entities.Comment;
import com.example.taskmanagementapp.entities.Task;
import com.example.taskmanagementapp.entities.User;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface CommentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "text", source = "commentDto.text")
    @Mapping(target = "task", source = "task")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "timestamp", expression = "java(java.time.LocalDateTime.now())")
    Comment toAddComment(CommentRequest commentDto, Task task, User user);

    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "userId", source = "user.id")
    CommentResponse toCommentDto(Comment comment);

    List<CommentResponse> toCommentDtoList(List<Comment> comments);
}
