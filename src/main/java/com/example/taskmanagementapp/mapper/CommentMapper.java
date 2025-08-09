package com.example.taskmanagementapp.mapper;

import com.example.taskmanagementapp.config.MapperConfig;
import com.example.taskmanagementapp.dto.comment.request.CommentRequest;
import com.example.taskmanagementapp.dto.comment.response.CommentResponse;
import com.example.taskmanagementapp.entity.Comment;
import com.example.taskmanagementapp.entity.Task;
import com.example.taskmanagementapp.entity.User;
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
