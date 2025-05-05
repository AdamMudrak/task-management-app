package com.example.taskmanagementapp.services.impl;

import com.example.taskmanagementapp.dtos.comment.request.AddCommentDto;
import com.example.taskmanagementapp.dtos.comment.request.UpdateCommentDto;
import com.example.taskmanagementapp.dtos.comment.response.CommentDto;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.services.CommentService;
import java.util.List;

public class CommentServiceImpl implements CommentService {
    @Override
    public CommentDto addComment(User authenticatedUser, AddCommentDto commentDto) {
        return null;
    }

    @Override
    public CommentDto updateComment(User authenticatedUser, UpdateCommentDto commentDto) {
        return null;
    }

    @Override
    public List<CommentDto> getAllComments(User authenticatedUser, Long taskId) {
        return List.of();
    }
}
