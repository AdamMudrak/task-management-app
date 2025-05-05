package com.example.taskmanagementapp.services;

import com.example.taskmanagementapp.dtos.comment.request.AddCommentDto;
import com.example.taskmanagementapp.dtos.comment.request.UpdateCommentDto;
import com.example.taskmanagementapp.dtos.comment.response.CommentDto;
import com.example.taskmanagementapp.entities.User;
import java.util.List;

public interface CommentService {
    CommentDto addComment(User authenticatedUser,
                          AddCommentDto commentDto);

    CommentDto updateComment(User authenticatedUser,
                             UpdateCommentDto commentDto);

    List<CommentDto> getAllComments(User authenticatedUser, Long taskId);
}
