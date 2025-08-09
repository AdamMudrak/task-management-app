package com.example.taskmanagementapp.service;

import com.example.taskmanagementapp.dto.comment.request.ColorDto;
import com.example.taskmanagementapp.dto.label.request.LabelRequest;
import com.example.taskmanagementapp.dto.label.request.UpdateLabelRequest;
import com.example.taskmanagementapp.dto.label.response.LabelResponse;
import com.example.taskmanagementapp.entity.User;
import com.example.taskmanagementapp.exception.ForbiddenException;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface LabelService {
    LabelResponse createLabel(User user, ColorDto colorDto, LabelRequest labelDto);

    LabelResponse updateLabel(Long authenticatedUserId, ColorDto colorDto,
                              UpdateLabelRequest labelDto, Long labelId);

    LabelResponse getLabelById(Long authenticatedUserId, Long id);

    List<LabelResponse> getAllLabels(Long authenticatedUserId, Pageable pageable);

    void deleteLabelById(Long authenticatedUserId, Long id);

    void attachLabelToTask(Long authenticatedUserId, Long taskId, Long labelId)
            throws ForbiddenException;

    void detachLabelFromTask(Long authenticatedUserId, Long taskId, Long labelId)
            throws ForbiddenException;
}
