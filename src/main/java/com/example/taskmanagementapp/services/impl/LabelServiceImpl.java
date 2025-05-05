package com.example.taskmanagementapp.services.impl;

import com.example.taskmanagementapp.dtos.comment.request.ColorDto;
import com.example.taskmanagementapp.dtos.label.request.AddLabelDto;
import com.example.taskmanagementapp.dtos.label.request.UpdateLabelDto;
import com.example.taskmanagementapp.dtos.label.response.LabelDto;
import com.example.taskmanagementapp.dtos.task.response.TaskDto;
import com.example.taskmanagementapp.entities.Label;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.notfoundexceptions.EntityNotFoundException;
import com.example.taskmanagementapp.mappers.LabelMapper;
import com.example.taskmanagementapp.repositories.label.LabelRepository;
import com.example.taskmanagementapp.repositories.project.ProjectRepository;
import com.example.taskmanagementapp.repositories.task.TaskRepository;
import com.example.taskmanagementapp.services.LabelService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LabelServiceImpl implements LabelService {
    private final LabelRepository labelRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final LabelMapper labelMapper;

    @Override
    public LabelDto createLabel(User user, ColorDto colorDto, AddLabelDto labelDto) {
        return labelMapper.toLabelDto(
                labelRepository.save(
                        labelMapper.toAddLabel(user, labelDto, colorDto)));
    }

    @Override
    public LabelDto updateLabel(User user, ColorDto colorDto,
                                UpdateLabelDto labelDto, Long labelId) {
        Label label = labelRepository.findByIdAndUserId(labelId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No label with id " + labelId + " for user with id " + user.getId()));
        return labelMapper.toLabelDto(
                labelRepository.save(
                        labelMapper.toUpdateLabel(label, labelDto, colorDto)));
    }

    @Override
    public LabelDto getLabelById(User user, Long id) {
        return labelMapper.toLabelDto(labelRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No label with id " + id + " for user with id " + user.getId())));
    }

    @Override
    public List<LabelDto> getAllLabels(User user, Pageable pageable) {
        return labelMapper.toLabelDtoList(
                labelRepository.findAllByUserId(user.getId(), pageable).getContent());
    }

    @Override
    public void deleteLabelById(User user, Long id) {
        if (labelRepository.existsByUserIdAndId(user.getId(), id)) {
            labelRepository.deleteById(id);
        } else {
            throw new EntityNotFoundException(
                    "No label with id " + id + " for user with id " + user.getId());
        }
    }

    @Override
    public List<TaskDto> attachLabelToTask(User user, Long taskId, Long labelId) {
        return List.of();
    }

    @Override
    public List<TaskDto> detachLabelFromTask(User user, Long taskId, Long labelId) {
        return List.of();
    }

    @Override
    public List<TaskDto> getTasksWithLabel(User user, Long labelId) {
        return List.of();
    }

    @Override
    public List<TaskDto> getTaskWithLabelColor(User user, ColorDto colorDto) {
        return List.of();
    }
}
