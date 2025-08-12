package com.example.taskmanagementapp.service.impl;

import com.example.taskmanagementapp.dto.comment.request.ColorDto;
import com.example.taskmanagementapp.dto.label.request.LabelRequest;
import com.example.taskmanagementapp.dto.label.request.UpdateLabelRequest;
import com.example.taskmanagementapp.dto.label.response.LabelResponse;
import com.example.taskmanagementapp.entity.Label;
import com.example.taskmanagementapp.entity.Task;
import com.example.taskmanagementapp.entity.User;
import com.example.taskmanagementapp.exception.EntityNotFoundException;
import com.example.taskmanagementapp.exception.ForbiddenException;
import com.example.taskmanagementapp.mapper.LabelMapper;
import com.example.taskmanagementapp.repository.LabelRepository;
import com.example.taskmanagementapp.repository.TaskRepository;
import com.example.taskmanagementapp.service.LabelService;
import com.example.taskmanagementapp.service.utils.ProjectAuthorityUtil;
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
    private final LabelMapper labelMapper;
    private final ProjectAuthorityUtil projectAuthorityUtil;

    @Override
    public LabelResponse createLabel(User user, ColorDto colorDto, LabelRequest labelDto) {
        return labelMapper.toLabelDto(
                labelRepository.save(
                        labelMapper.toAddLabel(user, labelDto, colorDto)));
    }

    @Override
    public LabelResponse updateLabel(Long authenticatedUserId, ColorDto colorDto,
                                     UpdateLabelRequest labelDto, Long labelId) {
        Label label = getLabelByIdAndUserId(labelId, authenticatedUserId);
        if (colorDto != null) {
            label.setColor(Label.Color.valueOf(colorDto.name()));
        }
        if (labelDto.name() != null && !labelDto.name().isBlank()) {
            label.setName(labelDto.name());
        }
        return labelMapper.toLabelDto(labelRepository.save(label));
    }

    @Override
    public LabelResponse getLabelById(Long authenticatedUserId, Long id) {
        return labelMapper.toLabelDto(labelRepository.findByIdAndUserId(id, authenticatedUserId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No label with id " + id + " for user with id " + authenticatedUserId)));
    }

    @Override
    public List<LabelResponse> getAllLabels(Long authenticatedUserId, Pageable pageable) {
        return labelMapper.toLabelDtoList(
                labelRepository.findAllByUserId(authenticatedUserId, pageable).getContent());
    }

    @Override
    public void deleteLabelById(Long authenticatedUserId, Long id) {
        if (labelRepository.existsByIdAndUserId(id, authenticatedUserId)) {
            labelRepository.deleteById(id);
        } else {
            throw new EntityNotFoundException(
                    "No label with id " + id + " for user with id " + authenticatedUserId);
        }
    }

    @Override
    public void attachLabelToTask(Long authenticatedUserId, Long taskId, Long labelId)
                                                        throws ForbiddenException {
        Label thisLabel = getLabelByIdAndUserId(labelId, authenticatedUserId);
        Task thisTask = getTaskById(taskId);
        isUserAssignee(thisTask, authenticatedUserId, Action.ATTACH);
        Long thisProjectId = thisTask.getProject().getId();

        if (projectAuthorityUtil.hasAnyAuthority(thisProjectId, authenticatedUserId)) {
            thisLabel.getTasks().add(thisTask);
            labelRepository.save(thisLabel);
        } else {
            throw new ForbiddenException("You can't attach label " + labelId
                    + " to task " + taskId + " since you are not in project " + thisProjectId);
        }
    }

    @Override
    public void detachLabelFromTask(Long authenticatedUserId, Long taskId, Long labelId)
                                                throws ForbiddenException {
        Label thisLabel = getLabelByIdAndUserId(labelId, authenticatedUserId);
        Task thisTask = getTaskById(taskId);
        isUserAssignee(thisTask, authenticatedUserId, Action.DETACH);
        Long thisProjectId = thisTask.getProject().getId();

        if (projectAuthorityUtil.hasAnyAuthority(thisProjectId, authenticatedUserId)) {
            thisLabel.getTasks().remove(thisTask);
            labelRepository.save(thisLabel);
        } else {
            throw new ForbiddenException("You can't detach label " + labelId
                    + " from task " + taskId + " since you are not in project " + thisProjectId);
        }
    }

    private Label getLabelByIdAndUserId(Long id, Long userId) {
        return labelRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No label with id " + id + " for user with id "
                                + userId));
    }

    private Task getTaskById(Long id) {
        return taskRepository.findByIdNotDeleted(
                id).orElseThrow(
                    () -> new EntityNotFoundException(
                        "No active task with id " + id));
    }

    private void isUserAssignee(Task task, Long userId, Action action) throws ForbiddenException {
        if (!task.getAssignee().getId().equals(userId)) {
            switch (action) {
                case ATTACH -> throw new ForbiddenException(
                        "You can attach labels only to tasks you are assigned to");
                case DETACH -> throw new ForbiddenException("You can detach labels only"
                        + " from tasks you are assigned to");
                default -> throw new EntityNotFoundException("No such action found " + action);
            }
        }
    }

    private enum Action {
        ATTACH,
        DETACH
    }
}
