package com.example.taskmanagementapp.controller;

import static com.example.taskmanagementapp.constant.Constants.CODE_200;
import static com.example.taskmanagementapp.constant.Constants.CODE_201;
import static com.example.taskmanagementapp.constant.Constants.CODE_204;
import static com.example.taskmanagementapp.constant.controller.CommentControllerConstants.ADD_COMMENT_SUMMARY;
import static com.example.taskmanagementapp.constant.controller.CommentControllerConstants.COMMENTS_API_DESCRIPTION;
import static com.example.taskmanagementapp.constant.controller.CommentControllerConstants.COMMENTS_API_NAME;
import static com.example.taskmanagementapp.constant.controller.CommentControllerConstants.DELETE_COMMENT_SUMMARY;
import static com.example.taskmanagementapp.constant.controller.CommentControllerConstants.GET_COMMENTS_SUMMARY;
import static com.example.taskmanagementapp.constant.controller.CommentControllerConstants.PAGEABLE_EXAMPLE;
import static com.example.taskmanagementapp.constant.controller.CommentControllerConstants.SUCCESSFULLY_ADDED_COMMENT;
import static com.example.taskmanagementapp.constant.controller.CommentControllerConstants.SUCCESSFULLY_DELETED_COMMENT;
import static com.example.taskmanagementapp.constant.controller.CommentControllerConstants.SUCCESSFULLY_GOT_COMMENTS;
import static com.example.taskmanagementapp.constant.controller.CommentControllerConstants.SUCCESSFULLY_UPDATED_COMMENT;
import static com.example.taskmanagementapp.constant.controller.CommentControllerConstants.UPDATE_COMMENT_SUMMARY;

import com.example.taskmanagementapp.dto.comment.request.CommentRequest;
import com.example.taskmanagementapp.dto.comment.request.UpdateCommentRequest;
import com.example.taskmanagementapp.dto.comment.response.CommentResponse;
import com.example.taskmanagementapp.entity.User;
import com.example.taskmanagementapp.exception.ForbiddenException;
import com.example.taskmanagementapp.service.CommentService;
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
@RequestMapping("/comments")
@RequiredArgsConstructor
@Validated
public class CommentController {
    private final CommentService commentService;

    @Operation(summary = ADD_COMMENT_SUMMARY)
    @ApiResponse(responseCode = CODE_201, description =
            SUCCESSFULLY_ADDED_COMMENT)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CommentResponse addComment(@AuthenticationPrincipal User user,
                                      @RequestBody @Valid CommentRequest addCommentDto)
                                                throws ForbiddenException {
        return commentService.addComment(user, addCommentDto);
    }

    @Operation(summary = UPDATE_COMMENT_SUMMARY)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_UPDATED_COMMENT)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/{commentId}")
    public CommentResponse updateComment(@AuthenticationPrincipal User user,
                                         @RequestBody @Valid UpdateCommentRequest updateCommentDto,
                                         @PathVariable @Positive Long commentId)
                                                                throws ForbiddenException {
        return commentService.updateComment(user.getId(), updateCommentDto, commentId);
    }

    @Operation(summary = GET_COMMENTS_SUMMARY)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_GOT_COMMENTS)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{taskId}")
    public List<CommentResponse> getCommentsForTask(@AuthenticationPrincipal User user,
                                                    @PathVariable @Positive Long taskId,
                                                    @Parameter(example = PAGEABLE_EXAMPLE)
                                                   Pageable pageable) throws ForbiddenException {
        return commentService.getAllComments(user.getId(), taskId, pageable);
    }

    @Operation(summary = DELETE_COMMENT_SUMMARY)
    @ApiResponse(responseCode = CODE_204, description =
            SUCCESSFULLY_DELETED_COMMENT)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{commentId}")
    public void deleteComment(@AuthenticationPrincipal User user,
                                    @PathVariable @Positive Long commentId)
            throws ForbiddenException {
        commentService.deleteComment(user.getId(), commentId);
    }
}
