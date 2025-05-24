package com.example.taskmanagementapp.services;

import com.example.taskmanagementapp.dtos.comment.request.CommentRequest;
import com.example.taskmanagementapp.dtos.comment.request.UpdateCommentRequest;
import com.example.taskmanagementapp.dtos.comment.response.CommentResponse;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.ForbiddenException;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    CommentResponse addComment(User authenticatedUser,
                               CommentRequest commentDto) throws ForbiddenException;

    CommentResponse updateComment(User authenticatedUser,
                                  UpdateCommentRequest commentDto,
                                  Long commentId) throws ForbiddenException;

    List<CommentResponse> getAllComments(User authenticatedUser, Long taskId,
                                         Pageable pageable) throws ForbiddenException;

    void deleteComment(User authenticatedUser, Long commentId) throws ForbiddenException;
}
