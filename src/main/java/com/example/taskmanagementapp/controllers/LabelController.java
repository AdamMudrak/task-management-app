package com.example.taskmanagementapp.controllers;

import static com.example.taskmanagementapp.constants.Constants.CODE_200;
import static com.example.taskmanagementapp.constants.Constants.CODE_201;
import static com.example.taskmanagementapp.constants.Constants.ROLE_ADMIN;
import static com.example.taskmanagementapp.constants.Constants.ROLE_USER;
import static com.example.taskmanagementapp.constants.controllers.LabelControllerConstants.ATTACH_LABEL_TO_TASK;
import static com.example.taskmanagementapp.constants.controllers.LabelControllerConstants.CREATE_LABEL_SUMMARY;
import static com.example.taskmanagementapp.constants.controllers.LabelControllerConstants.DELETE_LABEL_BY_ID_SUMMARY;
import static com.example.taskmanagementapp.constants.controllers.LabelControllerConstants.DETACH_LABEL_TO_TASK;
import static com.example.taskmanagementapp.constants.controllers.LabelControllerConstants.GET_LABELS_SUMMARY;
import static com.example.taskmanagementapp.constants.controllers.LabelControllerConstants.GET_LABEL_BY_ID_SUMMARY;
import static com.example.taskmanagementapp.constants.controllers.LabelControllerConstants.LABELS;
import static com.example.taskmanagementapp.constants.controllers.LabelControllerConstants.LABELS_API_DESCRIPTION;
import static com.example.taskmanagementapp.constants.controllers.LabelControllerConstants.LABELS_API_NAME;
import static com.example.taskmanagementapp.constants.controllers.LabelControllerConstants.LABEL_ID;
import static com.example.taskmanagementapp.constants.controllers.LabelControllerConstants.PAGEABLE_EXAMPLE;
import static com.example.taskmanagementapp.constants.controllers.LabelControllerConstants.SUCCESSFULLY_ATTACHED_LABEL;
import static com.example.taskmanagementapp.constants.controllers.LabelControllerConstants.SUCCESSFULLY_CREATED_LABEL;
import static com.example.taskmanagementapp.constants.controllers.LabelControllerConstants.SUCCESSFULLY_DELETED_LABEL;
import static com.example.taskmanagementapp.constants.controllers.LabelControllerConstants.SUCCESSFULLY_DETACHED_LABEL;
import static com.example.taskmanagementapp.constants.controllers.LabelControllerConstants.SUCCESSFULLY_GOT_LABEL;
import static com.example.taskmanagementapp.constants.controllers.LabelControllerConstants.SUCCESSFULLY_GOT_LABELS;
import static com.example.taskmanagementapp.constants.controllers.LabelControllerConstants.SUCCESSFULLY_UPDATED_LABEL;
import static com.example.taskmanagementapp.constants.controllers.LabelControllerConstants.TASK_ID_ATTACHMENT_ID_ATTACH;
import static com.example.taskmanagementapp.constants.controllers.LabelControllerConstants.TASK_ID_ATTACHMENT_ID_DETACH;
import static com.example.taskmanagementapp.constants.controllers.LabelControllerConstants.UPDATE_LABEL_SUMMARY;

import com.example.taskmanagementapp.dtos.comment.request.ColorDto;
import com.example.taskmanagementapp.dtos.label.request.AddLabelDto;
import com.example.taskmanagementapp.dtos.label.request.UpdateLabelDto;
import com.example.taskmanagementapp.dtos.label.response.LabelDto;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.forbidden.ForbiddenException;
import com.example.taskmanagementapp.services.LabelService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(LABELS)
@Tag(name = LABELS_API_NAME, description = LABELS_API_DESCRIPTION)
@RequiredArgsConstructor
@Validated
public class LabelController {
    private final LabelService labelService;

    @Operation(summary = CREATE_LABEL_SUMMARY)
    @ApiResponse(responseCode = CODE_201, description = SUCCESSFULLY_CREATED_LABEL)
    @PreAuthorize(ROLE_USER + " or "
            + ROLE_ADMIN)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    LabelDto createLabel(@AuthenticationPrincipal User user,
                         @RequestParam ColorDto colorDto,
                         @RequestBody @Valid AddLabelDto addLabelDto) {
        return labelService.createLabel(user, colorDto, addLabelDto);
    }

    @Operation(summary = UPDATE_LABEL_SUMMARY)
    @ApiResponse(responseCode = CODE_200, description = SUCCESSFULLY_UPDATED_LABEL)
    @PreAuthorize(ROLE_USER + " or "
            + ROLE_ADMIN)
    @PutMapping("/{labelId}")
    LabelDto updateLabel(@AuthenticationPrincipal User user,
                         @RequestParam(value = "colorDto", required = false) ColorDto colorDto,
                         @RequestBody @Valid UpdateLabelDto updateLabelDto,
                         @PathVariable @Positive Long labelId) {
        return labelService.updateLabel(user, colorDto, updateLabelDto, labelId);
    }

    @Operation(summary = GET_LABEL_BY_ID_SUMMARY)
    @ApiResponse(responseCode = CODE_200, description = SUCCESSFULLY_GOT_LABEL)
    @PreAuthorize(ROLE_USER + " or "
            + ROLE_ADMIN)
    @GetMapping(LABEL_ID)
    LabelDto getLabel(@AuthenticationPrincipal User user,
                         @PathVariable @Positive Long labelId) {
        return labelService.getLabelById(user, labelId);
    }

    @Operation(summary = GET_LABELS_SUMMARY)
    @ApiResponse(responseCode = CODE_200, description = SUCCESSFULLY_GOT_LABELS)
    @PreAuthorize(ROLE_USER + " or "
            + ROLE_ADMIN)
    @GetMapping()
    List<LabelDto> getLabels(@AuthenticationPrincipal User user,
                             @Parameter(example = PAGEABLE_EXAMPLE) Pageable pageable) {
        return labelService.getAllLabels(user, pageable);
    }

    @Operation(summary = DELETE_LABEL_BY_ID_SUMMARY)
    @ApiResponse(responseCode = CODE_200, description = SUCCESSFULLY_DELETED_LABEL)
    @PreAuthorize(ROLE_USER + " or "
            + ROLE_ADMIN)
    @DeleteMapping(LABEL_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteLabelById(@AuthenticationPrincipal User user,
                         @PathVariable @Positive Long labelId) {
        labelService.deleteLabelById(user, labelId);
    }

    @Operation(summary = ATTACH_LABEL_TO_TASK)
    @ApiResponse(responseCode = CODE_200, description = SUCCESSFULLY_ATTACHED_LABEL)
    @PreAuthorize(ROLE_USER + " or "
            + ROLE_ADMIN)
    @PutMapping(TASK_ID_ATTACHMENT_ID_ATTACH)
    void attachLabelToTask(@AuthenticationPrincipal User user,
                           @PathVariable @Positive Long taskId,
                           @PathVariable @Positive Long labelId) throws ForbiddenException {
        labelService.attachLabelToTask(user, taskId, labelId);
    }

    @Operation(summary = DETACH_LABEL_TO_TASK)
    @ApiResponse(responseCode = CODE_200, description = SUCCESSFULLY_DETACHED_LABEL)
    @PreAuthorize(ROLE_USER + " or "
            + ROLE_ADMIN)
    @PutMapping(TASK_ID_ATTACHMENT_ID_DETACH)
    void detachLabelFromTask(@AuthenticationPrincipal User user,
                           @PathVariable @Positive Long taskId,
                           @PathVariable @Positive Long labelId) throws ForbiddenException {
        labelService.detachLabelFromTask(user, taskId, labelId);
    }
}
