package com.example.taskmanagementapp.services.impl;

import com.example.taskmanagementapp.dtos.comment.request.ColorDto;
import com.example.taskmanagementapp.dtos.label.request.LabelRequest;
import com.example.taskmanagementapp.dtos.label.request.UpdateLabelRequest;
import com.example.taskmanagementapp.dtos.label.response.LabelResponse;
import com.example.taskmanagementapp.entities.Label;
import com.example.taskmanagementapp.entities.Task;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.EntityNotFoundException;
import com.example.taskmanagementapp.exceptions.ForbiddenException;
import com.example.taskmanagementapp.mappers.LabelMapper;
import com.example.taskmanagementapp.repositories.LabelRepository;
import com.example.taskmanagementapp.repositories.ProjectRepository;
import com.example.taskmanagementapp.repositories.TaskRepository;
import com.example.taskmanagementapp.services.LabelService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LabelServiceImpl implements LabelService {
    private final LabelRepository labelRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final LabelMapper labelMapper;

    @Override
    public LabelResponse createLabel(User user, ColorDto colorDto, LabelRequest labelDto) {
        return labelMapper.toLabelDto(
                labelRepository.save(
                        labelMapper.toAddLabel(user, labelDto, colorDto)));
    }

    @Override
    public LabelResponse updateLabel(User user, ColorDto colorDto,
                                     UpdateLabelRequest labelDto, Long labelId) {
        Label label = labelRepository.findByIdAndUserId(labelId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No label with id " + labelId + " for user with id " + user.getId()));
        if (colorDto != null) {
            label.setColor(Label.Color.valueOf(colorDto.name()));
        }
        if (labelDto.name() != null && !labelDto.name().isBlank()) {
            label.setName(labelDto.name());
        }
        return labelMapper.toLabelDto(labelRepository.save(label));
    }

    @Override
    public LabelResponse getLabelById(User user, Long id) {
        return labelMapper.toLabelDto(labelRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No label with id " + id + " for user with id " + user.getId())));
    }

    @Override
    public List<LabelResponse> getAllLabels(User user, Pageable pageable) {
        return labelMapper.toLabelDtoList(
                labelRepository.findAllByUserId(user.getId(), pageable).getContent());
    }

    @Override
    public void deleteLabelById(User user, Long id) {
        if (labelRepository.existsByIdAndUserId(id, user.getId())) {
            labelRepository.deleteById(id);
        } else {
            throw new EntityNotFoundException(
                    "No label with id " + id + " for user with id " + user.getId());
        }
    }

    @Override
    public void attachLabelToTask(User user, Long taskId, Long labelId)
                                                        throws ForbiddenException {
        Label thisLabel = labelRepository.findByIdAndUserId(labelId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No label with id " + labelId + " for user with id " + user.getId()));

        Task thisTask = taskRepository.findByIdNotDeleted(
                taskId).orElseThrow(
                    () -> new EntityNotFoundException(
                        "No active task with id " + taskId));

        if (!thisTask.getAssignee().getId().equals(user.getId())) {
            throw new ForbiddenException("You can attach labels only to tasks you are assigned to");
        }
        Long thisProjectId = thisTask.getProject().getId();

        if (projectRepository.isUserOwner(thisProjectId, user.getId())
                || projectRepository.isUserEmployee(thisProjectId, user.getId())
                || projectRepository.isUserManager(thisProjectId, user.getId())) {
            thisLabel.getTasks().add(thisTask);
            labelRepository.save(thisLabel);
        } else {
            throw new ForbiddenException("You can't attach label " + labelId
                    + " to task " + taskId + " since you are not in project " + thisProjectId);
        }
    }

    @Override
    public void detachLabelFromTask(User user, Long taskId, Long labelId)
                                                throws ForbiddenException {
        Label thisLabel = labelRepository.findByIdAndUserId(labelId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No label with id " + labelId + " for user with id " + user.getId()));

        Task thisTask = taskRepository.findByIdNotDeleted(
                taskId).orElseThrow(
                    () -> new EntityNotFoundException(
                        "No active task with id " + taskId));

        if (!thisTask.getAssignee().getId().equals(user.getId())) {
            throw new ForbiddenException("You can detach labels only"
                    + " from tasks you are assigned to");
        }
        Long thisProjectId = thisTask.getProject().getId();

        if (projectRepository.isUserOwner(thisProjectId, user.getId())
                || projectRepository.isUserEmployee(thisProjectId, user.getId())
                || projectRepository.isUserManager(thisProjectId, user.getId())) {
            thisLabel.getTasks().remove(thisTask);
            labelRepository.save(thisLabel);
        } else {
            throw new ForbiddenException("You can't detach label " + labelId
                    + " from task " + taskId + " since you are not in project " + thisProjectId);
        }
    }
}
