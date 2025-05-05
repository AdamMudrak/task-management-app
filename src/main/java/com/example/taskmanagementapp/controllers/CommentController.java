package com.example.taskmanagementapp.controllers;

import com.example.taskmanagementapp.dtos.comment.request.AddCommentDto;
import com.example.taskmanagementapp.dtos.comment.request.UpdateCommentDto;
import com.example.taskmanagementapp.dtos.comment.response.CommentDto;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.forbidden.ForbiddenException;
import com.example.taskmanagementapp.services.CommentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public CommentDto addComment(@AuthenticationPrincipal User user,
                                 AddCommentDto addCommentDto) throws ForbiddenException {
        return commentService.addComment(user, addCommentDto);
    }

    @PutMapping("/{commentId}")
    public CommentDto updateComment(@AuthenticationPrincipal User user,
                                    UpdateCommentDto updateCommentDto,
                                    @PathVariable Long commentId) throws ForbiddenException {
        return commentService.updateComment(user, updateCommentDto, commentId);
    }

    @GetMapping("/{taskId}")
    public List<CommentDto> getCommentsForTask(@AuthenticationPrincipal User user,
                                               @PathVariable Long taskId,
                                               Pageable pageable) throws ForbiddenException {
        return commentService.getAllComments(user, taskId, pageable);
    }

    @DeleteMapping("/{commentId}")
    public void updateComment(@AuthenticationPrincipal User user,
                                    @PathVariable Long commentId) throws ForbiddenException {
        commentService.deleteCommentById(user, commentId);
    }
}
