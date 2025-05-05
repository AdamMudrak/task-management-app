package com.example.taskmanagementapp.services.impl;

import com.example.taskmanagementapp.dtos.comment.request.AddCommentDto;
import com.example.taskmanagementapp.dtos.comment.request.UpdateCommentDto;
import com.example.taskmanagementapp.dtos.comment.response.CommentDto;
import com.example.taskmanagementapp.entities.Comment;
import com.example.taskmanagementapp.entities.Task;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.forbidden.ForbiddenException;
import com.example.taskmanagementapp.exceptions.notfoundexceptions.EntityNotFoundException;
import com.example.taskmanagementapp.mappers.CommentMapper;
import com.example.taskmanagementapp.repositories.comment.CommentRepository;
import com.example.taskmanagementapp.repositories.project.ProjectRepository;
import com.example.taskmanagementapp.repositories.task.TaskRepository;
import com.example.taskmanagementapp.services.CommentService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final CommentMapper commentMapper;

    @Override
    public CommentDto addComment(User authenticatedUser, AddCommentDto commentDto)
                                                        throws ForbiddenException {
        Task thisTask = taskRepository.findByIdNotDeleted(
                commentDto.taskId()).orElseThrow(
                        () -> new EntityNotFoundException(
                                "No active task with id " + commentDto.taskId()));
        Long thisProjectId = thisTask.getProject().getId();

        if (projectRepository.isUserEmployee(thisProjectId, authenticatedUser.getId())
                || projectRepository.isUserEmployee(thisProjectId, authenticatedUser.getId())
                || projectRepository.isUserManager(thisProjectId, authenticatedUser.getId())) {
            Comment addComment = commentMapper.toAddComment(commentDto);
            addComment.setTask(thisTask);
            addComment.setUser(authenticatedUser);
            addComment.setTimestamp(LocalDateTime.now());
            return commentMapper.toCommentDto(commentRepository.save(addComment));
        } else {
            throw new ForbiddenException("You can't add comments to task " + commentDto.taskId()
                + " since you are not participant in project " + thisProjectId);
        }
    }

    @Override
    public CommentDto updateComment(User authenticatedUser,
                                    UpdateCommentDto commentDto, Long commentId)
                                                        throws ForbiddenException {
        Comment thisComment = commentRepository.findById(commentId).orElseThrow(
                () -> new EntityNotFoundException("No comment with id " + commentId));
        if (!thisComment.getUser().getId().equals(authenticatedUser.getId())) {
            throw new ForbiddenException("You cannot update other people's comments");
        }
        Long thisCommentTaskId = thisComment.getTask().getId();

        Task thisTask = taskRepository.findByIdNotDeleted(
                thisCommentTaskId).orElseThrow(() -> new EntityNotFoundException(
                        "No active task with id " + thisCommentTaskId));
        Long thisProjectId = thisTask.getProject().getId();

        if (projectRepository.isUserEmployee(thisProjectId, authenticatedUser.getId())
                || projectRepository.isUserEmployee(thisProjectId, authenticatedUser.getId())
                || projectRepository.isUserManager(thisProjectId, authenticatedUser.getId())) {
            thisComment.setText(commentDto.text());
            return commentMapper.toCommentDto(commentRepository.save(thisComment));
        } else {
            throw new ForbiddenException("You can't add comments to task " + commentId
                    + " since you are not participant in project " + thisProjectId);
        }
    }

    @Override
    public List<CommentDto> getAllComments(User authenticatedUser, Long taskId,
                                           Pageable pageable) throws ForbiddenException {
        Task thisTask = taskRepository.findByIdNotDeleted(
                taskId).orElseThrow(() -> new EntityNotFoundException(
                "No active task with id " + taskId));
        Long thisProjectId = thisTask.getProject().getId();

        if (projectRepository.isUserEmployee(thisProjectId, authenticatedUser.getId())
                || projectRepository.isUserEmployee(thisProjectId, authenticatedUser.getId())
                || projectRepository.isUserManager(thisProjectId, authenticatedUser.getId())) {
            return commentMapper.toCommentDtoList(commentRepository
                    .findAllByTaskId(taskId, pageable).getContent());
        } else {
            throw new ForbiddenException("You can't get comments for task " + taskId
                    + " since you are not participant in project " + thisProjectId);
        }
    }
}
