package com.example.taskmanagementapp.controllers;

import static com.example.taskmanagementapp.constants.Constants.CODE_200;
import static com.example.taskmanagementapp.constants.Constants.CODE_201;
import static com.example.taskmanagementapp.constants.Constants.CODE_204;
import static com.example.taskmanagementapp.constants.Constants.ROLE_ADMIN;
import static com.example.taskmanagementapp.constants.Constants.ROLE_USER;
import static com.example.taskmanagementapp.constants.controllers.CommentControllerConstants.ADD_COMMENT_SUMMARY;
import static com.example.taskmanagementapp.constants.controllers.CommentControllerConstants.COMMENTS;
import static com.example.taskmanagementapp.constants.controllers.CommentControllerConstants.COMMENTS_API_DESCRIPTION;
import static com.example.taskmanagementapp.constants.controllers.CommentControllerConstants.COMMENTS_API_NAME;
import static com.example.taskmanagementapp.constants.controllers.CommentControllerConstants.COMMENT_ID;
import static com.example.taskmanagementapp.constants.controllers.CommentControllerConstants.DELETE_COMMENT_SUMMARY;
import static com.example.taskmanagementapp.constants.controllers.CommentControllerConstants.GET_COMMENTS_SUMMARY;
import static com.example.taskmanagementapp.constants.controllers.CommentControllerConstants.PAGEABLE_EXAMPLE;
import static com.example.taskmanagementapp.constants.controllers.CommentControllerConstants.SUCCESSFULLY_ADDED_COMMENT;
import static com.example.taskmanagementapp.constants.controllers.CommentControllerConstants.SUCCESSFULLY_DELETED_COMMENT;
import static com.example.taskmanagementapp.constants.controllers.CommentControllerConstants.SUCCESSFULLY_GOT_COMMENTS;
import static com.example.taskmanagementapp.constants.controllers.CommentControllerConstants.SUCCESSFULLY_UPDATED_COMMENT;
import static com.example.taskmanagementapp.constants.controllers.CommentControllerConstants.TASK_ID;
import static com.example.taskmanagementapp.constants.controllers.CommentControllerConstants.UPDATE_COMMENT_SUMMARY;

import com.example.taskmanagementapp.dtos.comment.request.AddCommentDto;
import com.example.taskmanagementapp.dtos.comment.request.UpdateCommentDto;
import com.example.taskmanagementapp.dtos.comment.response.CommentDto;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.forbidden.ForbiddenException;
import com.example.taskmanagementapp.services.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = COMMENTS_API_NAME, description = COMMENTS_API_DESCRIPTION)
@RequestMapping(COMMENTS)
@RequiredArgsConstructor
@Validated
public class CommentController {
    private final CommentService commentService;

    @Operation(summary = ADD_COMMENT_SUMMARY)
    @ApiResponse(responseCode = CODE_201, description =
            SUCCESSFULLY_ADDED_COMMENT)
    @PreAuthorize(ROLE_USER + " or "
            + ROLE_ADMIN)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CommentDto addComment(@AuthenticationPrincipal User user,
                                 @RequestBody @Valid AddCommentDto addCommentDto)
                                                throws ForbiddenException {
        return commentService.addComment(user, addCommentDto);
    }

    @Operation(summary = UPDATE_COMMENT_SUMMARY)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_UPDATED_COMMENT)
    @PreAuthorize(ROLE_USER + " or "
            + ROLE_ADMIN)
    @PutMapping(COMMENT_ID)
    public CommentDto updateComment(@AuthenticationPrincipal User user,
                                    @RequestBody @Valid UpdateCommentDto updateCommentDto,
                                    @PathVariable @Positive Long commentId)
                                                                throws ForbiddenException {
        return commentService.updateComment(user, updateCommentDto, commentId);
    }

    @Operation(summary = GET_COMMENTS_SUMMARY)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_GOT_COMMENTS)
    @PreAuthorize(ROLE_USER + " or "
            + ROLE_ADMIN)
    @GetMapping(TASK_ID)
    public List<CommentDto> getCommentsForTask(@AuthenticationPrincipal User user,
                                               @PathVariable @Positive Long taskId,
                                               @Parameter(example = PAGEABLE_EXAMPLE)
                                                   Pageable pageable) throws ForbiddenException {
        return commentService.getAllComments(user, taskId, pageable);
    }

    @Operation(summary = DELETE_COMMENT_SUMMARY)
    @ApiResponse(responseCode = CODE_204, description =
            SUCCESSFULLY_DELETED_COMMENT)
    @PreAuthorize(ROLE_USER + " or "
            + ROLE_ADMIN)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(COMMENT_ID)
    public void deleteComment(@AuthenticationPrincipal User user,
                                    @PathVariable @Positive Long commentId)
            throws ForbiddenException {
        commentService.deleteComment(user, commentId);
    }
}
