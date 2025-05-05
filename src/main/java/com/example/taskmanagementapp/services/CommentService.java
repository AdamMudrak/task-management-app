package com.example.taskmanagementapp.services;

import com.example.taskmanagementapp.dtos.comment.request.AddCommentDto;
import com.example.taskmanagementapp.dtos.comment.request.UpdateCommentDto;
import com.example.taskmanagementapp.dtos.comment.response.CommentDto;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.forbidden.ForbiddenException;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    CommentDto addComment(User authenticatedUser,
                          AddCommentDto commentDto) throws ForbiddenException;

    CommentDto updateComment(User authenticatedUser,
                             UpdateCommentDto commentDto,
                             Long commentId) throws ForbiddenException;

    List<CommentDto> getAllComments(User authenticatedUser, Long taskId,
                                    Pageable pageable) throws ForbiddenException;
}
