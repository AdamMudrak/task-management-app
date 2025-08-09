package com.example.taskmanagementapp.service;

import com.example.taskmanagementapp.dto.comment.request.CommentRequest;
import com.example.taskmanagementapp.dto.comment.request.UpdateCommentRequest;
import com.example.taskmanagementapp.dto.comment.response.CommentResponse;
import com.example.taskmanagementapp.entity.User;
import com.example.taskmanagementapp.exception.ForbiddenException;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    CommentResponse addComment(User authenticatedUser,
                               CommentRequest commentDto) throws ForbiddenException;

    CommentResponse updateComment(Long authenticatedUserId,
                                  UpdateCommentRequest commentDto,
                                  Long commentId) throws ForbiddenException;

    List<CommentResponse> getAllComments(Long authenticatedUserId, Long taskId,
                                         Pageable pageable) throws ForbiddenException;

    void deleteComment(Long authenticatedUserId, Long commentId) throws ForbiddenException;
}
