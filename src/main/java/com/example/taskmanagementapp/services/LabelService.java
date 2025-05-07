package com.example.taskmanagementapp.services;

import com.example.taskmanagementapp.dtos.comment.request.ColorDto;
import com.example.taskmanagementapp.dtos.label.request.AddLabelDto;
import com.example.taskmanagementapp.dtos.label.request.UpdateLabelDto;
import com.example.taskmanagementapp.dtos.label.response.LabelDto;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.forbidden.ForbiddenException;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface LabelService {
    LabelDto createLabel(User user, ColorDto colorDto, AddLabelDto labelDto);

    LabelDto updateLabel(User user, ColorDto colorDto, UpdateLabelDto labelDto, Long labelId);

    LabelDto getLabelById(User user, Long id);

    List<LabelDto> getAllLabels(User user, Pageable pageable);

    void deleteLabelById(User user, Long id);

    void attachLabelToTask(User user, Long taskId, Long labelId) throws ForbiddenException;

    void detachLabelFromTask(User user, Long taskId, Long labelId) throws ForbiddenException;
}
