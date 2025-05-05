package com.example.taskmanagementapp.mappers;

import com.example.taskmanagementapp.config.MapperConfig;
import com.example.taskmanagementapp.dtos.comment.request.AddCommentDto;
import com.example.taskmanagementapp.dtos.comment.response.CommentDto;
import com.example.taskmanagementapp.entities.Comment;
import com.example.taskmanagementapp.entities.Task;
import com.example.taskmanagementapp.entities.User;
import java.time.LocalDateTime;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface CommentMapper {
    default Comment toAddComment(AddCommentDto commentDto, Task task, User user) {
        Comment comment = new Comment();
        comment.setText(commentDto.text());
        comment.setTask(task);
        comment.setUser(user);
        comment.setTimestamp(LocalDateTime.now());
        return comment;
    }

    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "userId", source = "user.id")
    CommentDto toCommentDto(Comment comment);

    List<CommentDto> toCommentDtoList(List<Comment> comments);
}
