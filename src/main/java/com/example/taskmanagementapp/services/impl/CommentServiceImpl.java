package com.example.taskmanagementapp.services.impl;

import com.example.taskmanagementapp.dtos.comment.request.CommentRequest;
import com.example.taskmanagementapp.dtos.comment.request.UpdateCommentRequest;
import com.example.taskmanagementapp.dtos.comment.response.CommentResponse;
import com.example.taskmanagementapp.entities.Comment;
import com.example.taskmanagementapp.entities.Task;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.EntityNotFoundException;
import com.example.taskmanagementapp.exceptions.ForbiddenException;
import com.example.taskmanagementapp.mappers.CommentMapper;
import com.example.taskmanagementapp.repositories.CommentRepository;
import com.example.taskmanagementapp.repositories.TaskRepository;
import com.example.taskmanagementapp.services.CommentService;
import com.example.taskmanagementapp.services.utils.ProjectAuthorityUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final CommentMapper commentMapper;
    private final ProjectAuthorityUtil projectAuthorityUtil;

    @Override
    public CommentResponse addComment(User authenticatedUser, CommentRequest commentDto)
            throws ForbiddenException {
        Task thisTask = taskRepository.findByIdNotDeleted(
                commentDto.taskId()).orElseThrow(
                        () -> new EntityNotFoundException(
                        "No active task with id " + commentDto.taskId()));
        Long thisProjectId = thisTask.getProject().getId();

        if (projectAuthorityUtil.hasAnyAuthority(thisProjectId, authenticatedUser.getId())) {
            return commentMapper.toCommentDto(
                    commentRepository.save(
                            commentMapper.toAddComment(commentDto, thisTask, authenticatedUser)));
        } else {
            throw new ForbiddenException("You can't add comments to task " + commentDto.taskId()
                    + " since you are not participant in project " + thisProjectId);
        }
    }

    @Override
    public CommentResponse updateComment(Long authenticatedUserId,
                                         UpdateCommentRequest commentDto, Long commentId)
            throws ForbiddenException {
        Comment comment = getCommentByIdAndUserId(commentId, authenticatedUserId);
        Long commentTaskId = comment.getTask().getId();
        Long taskProjectId = getProjectIdForTaskWithId(commentTaskId);

        if (projectAuthorityUtil.hasAnyAuthority(taskProjectId, authenticatedUserId)) {
            comment.setText(commentDto.text());
            return commentMapper.toCommentDto(commentRepository.save(comment));
        } else {
            throw new ForbiddenException("You can't update comments for task " + commentTaskId
                    + " since you are not participant in project " + taskProjectId);
        }
    }

    @Override
    public List<CommentResponse> getAllComments(Long authenticatedUserId, Long taskId,
                                                Pageable pageable) throws ForbiddenException {
        Long thisProjectId = getProjectIdForTaskWithId(taskId);

        if (projectAuthorityUtil.hasAnyAuthority(thisProjectId, authenticatedUserId)) {
            return commentMapper.toCommentDtoList(commentRepository
                    .findAllByTaskId(taskId, pageable).getContent());
        } else {
            throw new ForbiddenException("You can't get comments for task " + taskId
                    + " since you are not participant in project " + thisProjectId);
        }
    }

    @Override
    public void deleteComment(Long authenticatedUserId, Long commentId) throws ForbiddenException {
        Comment thisComment = getCommentByIdAndUserId(commentId, authenticatedUserId);
        Long thisCommentTaskId = thisComment.getTask().getId();
        Long thisProjectId = getProjectIdForTaskWithId(thisCommentTaskId);

        if (projectAuthorityUtil.hasAnyAuthority(thisProjectId, authenticatedUserId)) {
            commentRepository.deleteById(commentId);
        } else {
            throw new ForbiddenException("You can't delete comments from task " + thisCommentTaskId
                    + " since you are not participant in project " + thisProjectId);
        }
    }

    private Comment getCommentByIdAndUserId(Long commentId, Long userId) {
        return commentRepository.findByIdAndUserId(commentId, userId).orElseThrow(
                () -> new EntityNotFoundException("No comment with id " + commentId
                        + " found for user " + userId));
    }

    private Long getProjectIdForTaskWithId(Long taskId) {
        return taskRepository.findByIdNotDeleted(
                taskId).orElseThrow(() -> new EntityNotFoundException(
                "No active task with id " + taskId)).getProject().getId();
    }
}
