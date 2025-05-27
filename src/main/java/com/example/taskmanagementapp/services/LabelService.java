package com.example.taskmanagementapp.services;

import com.example.taskmanagementapp.dtos.comment.request.ColorDto;
import com.example.taskmanagementapp.dtos.label.request.LabelRequest;
import com.example.taskmanagementapp.dtos.label.request.UpdateLabelRequest;
import com.example.taskmanagementapp.dtos.label.response.LabelResponse;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.ForbiddenException;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface LabelService {
    LabelResponse createLabel(User user, ColorDto colorDto, LabelRequest labelDto);

    LabelResponse updateLabel(User user, ColorDto colorDto,
                              UpdateLabelRequest labelDto, Long labelId);

    LabelResponse getLabelById(User user, Long id);

    List<LabelResponse> getAllLabels(User user, Pageable pageable);

    void deleteLabelById(User user, Long id);

    void attachLabelToTask(Long authenticatedUserId, Long taskId, Long labelId)
            throws ForbiddenException;

    void detachLabelFromTask(Long authenticatedUserId, Long taskId, Long labelId)
            throws ForbiddenException;
}
