package com.example.taskmanagementapp.services.impl;

import com.example.taskmanagementapp.dtos.task.request.CreateTaskDto;
import com.example.taskmanagementapp.dtos.task.request.TaskPriorityDto;
import com.example.taskmanagementapp.dtos.task.request.TaskStatusDto;
import com.example.taskmanagementapp.dtos.task.request.UpdateTaskDto;
import com.example.taskmanagementapp.dtos.task.response.TaskDto;
import com.example.taskmanagementapp.entities.Project;
import com.example.taskmanagementapp.entities.Task;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.ForbiddenException;
import com.example.taskmanagementapp.exceptions.EntityNotFoundException;
import com.example.taskmanagementapp.mappers.TaskMapper;
import com.example.taskmanagementapp.repositories.CommentRepository;
import com.example.taskmanagementapp.repositories.LabelRepository;
import com.example.taskmanagementapp.repositories.ProjectRepository;
import com.example.taskmanagementapp.repositories.TaskRepository;
import com.example.taskmanagementapp.repositories.UserRepository;
import com.example.taskmanagementapp.services.TaskService;
import com.example.taskmanagementapp.services.email.TaskAssignmentEmailService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final CommentRepository commentRepository;
    private final LabelRepository labelRepository;
    private final TaskAssignmentEmailService taskAssignmentEmailService;

    @Override
    public TaskDto createTask(User authenticatedUser,
                              CreateTaskDto createTaskDto,
                              TaskPriorityDto taskPriorityDto) throws ForbiddenException {
        Long projectId = createTaskDto.projectId();
        Long assigneeId = createTaskDto.assigneeId();

        Project project = projectRepository.findByIdNotDeleted(projectId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No active project with id " + projectId));
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new EntityNotFoundException("No user with id " + assigneeId));

        if (projectRepository.isUserManager(projectId, authenticatedUser.getId())
                || projectRepository.isUserOwner(projectId, authenticatedUser.getId())) {
            if (projectRepository.isUserManager(projectId, assigneeId)
                    || projectRepository.isUserOwner(projectId, assigneeId)
                    || projectRepository.isUserEmployee(projectId, assigneeId)) {
                Task createTask = taskMapper.toCreateTask(createTaskDto);
                createTask.setStatus(Task.Status.NOT_STARTED);
                createTask.setPriority(Task.Priority.valueOf(taskPriorityDto.name()));
                taskAssignmentEmailService.sendTaskAssignmentEmail(
                        assignee.getEmail(),
                        authenticatedUser.getEmail(),
                        createTask.getName(),
                        project.getName());
                return taskMapper.toTaskDto(taskRepository.save(createTask));
            } else {
                throw new ForbiddenException("User " + assigneeId + " is not assigned to project "
                        + projectId);
            }
        } else {
            throw new ForbiddenException("You have no permission to modify project " + projectId);
        }
    }

    @Override
    public List<TaskDto> getTasksForProject(User authenticatedUser,
                                            Long projectId,
                                            Pageable pageable)
            throws ForbiddenException {
        if (!projectRepository.existsByIdNotDeleted(projectId)) {
            throw new EntityNotFoundException("No active project with id " + projectId);
        }
        if (projectRepository.isUserEmployee(projectId, authenticatedUser.getId())
                || projectRepository.isUserOwner(projectId, authenticatedUser.getId())
                || projectRepository.isUserManager(projectId, authenticatedUser.getId())) {
            return taskMapper.toTaskDtoList(
                    taskRepository.findAllByProjectIdNonDeleted(projectId, pageable).getContent());
        } else {
            throw new ForbiddenException("You have no permission to access this project tasks");
        }
    }

    @Override
    public TaskDto getTaskById(User authenticatedUser, Long taskId) throws ForbiddenException {
        Task task = taskRepository.findByIdNotDeleted(taskId).orElseThrow(
                () -> new EntityNotFoundException("No active task with id " + taskId));
        Long thisTaskProjectId = task.getProject().getId();

        if (projectRepository.isUserOwner(thisTaskProjectId, authenticatedUser.getId())
                || projectRepository.isUserEmployee(thisTaskProjectId, authenticatedUser.getId())
                || projectRepository.isUserManager(thisTaskProjectId, authenticatedUser.getId())) {
            return taskMapper.toTaskDto(task);
        } else {
            throw new ForbiddenException("You have no permission to access this task");
        }
    }

    @Override
    public TaskDto updateTask(User authenticatedUser, UpdateTaskDto updateTaskDto, Long taskId,
                              TaskStatusDto taskStatusDto, TaskPriorityDto taskPriorityDto)
            throws ForbiddenException {
        Task task = taskRepository.findByIdNotDeleted(taskId).orElseThrow(
                () -> new EntityNotFoundException("No active task with id " + taskId));
        Long thisTaskProjectId = task.getProject().getId();

        if (projectRepository.isUserOwner(thisTaskProjectId, authenticatedUser.getId())
                || projectRepository.isUserManager(thisTaskProjectId, authenticatedUser.getId())) {
            updatePresentField(authenticatedUser, task, updateTaskDto,
                    taskStatusDto, taskPriorityDto);
            return taskMapper.toTaskDto(taskRepository.save(task));
        } else {
            throw new ForbiddenException("You have no permission to update this task");
        }
    }

    @Override
    public void deleteTask(User authenticatedUser, Long taskId) throws ForbiddenException {
        Task task = taskRepository.findByIdNotDeleted(taskId).orElseThrow(
                () -> new EntityNotFoundException("No active task with id " + taskId));
        Long thisTaskProjectId = task.getProject().getId();

        if (projectRepository.isUserOwner(thisTaskProjectId, authenticatedUser.getId())
                || projectRepository.isUserManager(thisTaskProjectId, authenticatedUser.getId())) {
            commentRepository.deleteAllByTaskId(taskId);
            taskRepository.deleteById(taskId);
        } else {
            throw new ForbiddenException("You have no permission to delete this task");
        }
    }

    @Override
    public List<TaskDto> getTasksWithLabel(User user, Long labelId, Pageable pageable) {
        if (labelRepository.existsByIdAndUserId(labelId, user.getId())) {
            return taskMapper.toTaskDtoList(
                    taskRepository.findAllByLabelIdNonDeleted(labelId, pageable).getContent());
        } else {
            throw new EntityNotFoundException(
                    "No label with id " + labelId + " for user with id " + user.getId());
        }
    }

    private void updatePresentField(User authenticatedUser,
                                    Task task,
                                    UpdateTaskDto updateTaskDto,
                                    TaskStatusDto taskStatusDto,
                                    TaskPriorityDto taskPriorityDto) throws ForbiddenException {
        if (updateTaskDto.name() != null
                && !updateTaskDto.name().isBlank()
                && !updateTaskDto.name().equals(task.getName())) {
            task.setName(updateTaskDto.name());
        }
        if (updateTaskDto.description() != null
                && !updateTaskDto.description().isBlank()
                && !updateTaskDto.description().equals(task.getDescription())) {
            task.setDescription(updateTaskDto.description());
        }
        if (updateTaskDto.dueDate() != null
                && !updateTaskDto.dueDate().isEqual(task.getDueDate())) {
            task.setDueDate(updateTaskDto.dueDate());
        }

        if (updateTaskDto.projectId() != null) {
            Project project = projectRepository.findById(updateTaskDto.projectId()).orElseThrow(
                    () -> new EntityNotFoundException(
                            "No project with id " + updateTaskDto.projectId()));

            if (projectRepository.isUserOwner(updateTaskDto.projectId(), authenticatedUser.getId())
                    || projectRepository.isUserManager(
                            updateTaskDto.projectId(), authenticatedUser.getId())) {
                task.setProject(project);
            } else {
                throw new ForbiddenException(
                        "You have no permission to modify project " + project.getId());
            }
        }

        if (updateTaskDto.assigneeId() != null) {
            User assignee = userRepository.findById(updateTaskDto.assigneeId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "No user with id " + updateTaskDto.assigneeId()));

            if (!projectRepository.isUserEmployee(
                    task.getProject().getId(), updateTaskDto.assigneeId())) {
                throw new ForbiddenException("You can't assign employee "
                        + updateTaskDto.assigneeId() + " to task " + task.getId()
                        + " since they are not in project " + task.getProject().getId());
            }
            task.setAssignee(assignee);
            taskAssignmentEmailService.sendTaskAssignmentEmail(
                    assignee.getEmail(),
                    authenticatedUser.getEmail(),
                    task.getName(),
                    task.getProject().getName());
        }

        if (taskStatusDto != null) {
            task.setStatus(Task.Status.valueOf(taskStatusDto.name()));
        }

        if (taskPriorityDto != null) {
            task.setPriority(Task.Priority.valueOf(taskPriorityDto.name()));
        }
    }
}
