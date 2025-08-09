package com.example.taskmanagementapp.service.impl;

import static com.example.taskmanagementapp.constant.security.SecurityConstants.NO_ACCESS_PERMISSION_FOR_PROJECT_TASKS;
import static com.example.taskmanagementapp.constant.security.SecurityConstants.NO_ACCESS_PERMISSION_FOR_TASK;
import static com.example.taskmanagementapp.constant.security.SecurityConstants.NO_PERMISSION_FOR_TASK_DELETION;
import static com.example.taskmanagementapp.service.utils.UpdateValueValidatorUtil.areDatesValid;
import static com.example.taskmanagementapp.service.utils.UpdateValueValidatorUtil.areStringsValid;

import com.example.taskmanagementapp.dto.task.request.TaskPriorityDto;
import com.example.taskmanagementapp.dto.task.request.TaskRequest;
import com.example.taskmanagementapp.dto.task.request.TaskStatusDto;
import com.example.taskmanagementapp.dto.task.request.UpdateTaskRequest;
import com.example.taskmanagementapp.dto.task.response.TaskResponse;
import com.example.taskmanagementapp.entity.Project;
import com.example.taskmanagementapp.entity.Task;
import com.example.taskmanagementapp.entity.User;
import com.example.taskmanagementapp.exception.EntityNotFoundException;
import com.example.taskmanagementapp.exception.ForbiddenException;
import com.example.taskmanagementapp.mapper.TaskMapper;
import com.example.taskmanagementapp.repository.CommentRepository;
import com.example.taskmanagementapp.repository.LabelRepository;
import com.example.taskmanagementapp.repository.ProjectRepository;
import com.example.taskmanagementapp.repository.TaskRepository;
import com.example.taskmanagementapp.repository.UserRepository;
import com.example.taskmanagementapp.service.TaskService;
import com.example.taskmanagementapp.service.email.TaskAssignmentEmailService;
import com.example.taskmanagementapp.service.utils.ProjectAuthorityUtil;
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
    private final ProjectAuthorityUtil projectAuthorityUtil;

    @Override
    public TaskResponse createTask(User authenticatedUser,
                                   TaskRequest createTaskDto,
                                   TaskPriorityDto taskPriorityDto) throws ForbiddenException {
        Long projectId = createTaskDto.projectId();
        Long assigneeId = createTaskDto.assigneeId();

        Project project = projectRepository.findByIdNotDeleted(projectId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No active project with id " + projectId));
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new EntityNotFoundException("No user with id " + assigneeId));

        if (projectAuthorityUtil.hasManagerialAuthority(projectId, authenticatedUser.getId())) {
            if (projectAuthorityUtil.hasAnyAuthority(projectId, assigneeId)) {
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
    public List<TaskResponse> getTasksForProject(Long authenticatedUserId,
                                                 Long projectId,
                                                 Pageable pageable)
            throws ForbiddenException {
        if (!projectRepository.existsByIdNotDeleted(projectId)) {
            throw new EntityNotFoundException("No active project with id " + projectId);
        }
        if (projectAuthorityUtil.hasAnyAuthority(projectId, authenticatedUserId)) {
            return taskMapper.toTaskDtoList(
                    taskRepository.findAllByProjectIdNonDeleted(projectId, pageable).getContent());
        } else {
            throw new ForbiddenException(NO_ACCESS_PERMISSION_FOR_PROJECT_TASKS);
        }
    }

    @Override
    public TaskResponse getTaskById(Long authenticatedUserId, Long taskId)
            throws ForbiddenException {
        Task task = taskRepository.findByIdNotDeleted(taskId).orElseThrow(
                () -> new EntityNotFoundException("No active task with id " + taskId));
        Long thisTaskProjectId = task.getProject().getId();

        if (projectAuthorityUtil.hasAnyAuthority(thisTaskProjectId, authenticatedUserId)) {
            return taskMapper.toTaskDto(task);
        } else {
            throw new ForbiddenException(NO_ACCESS_PERMISSION_FOR_TASK);
        }
    }

    @Override
    public TaskResponse updateTask(User authenticatedUser,
                                   UpdateTaskRequest updateTaskDto, Long taskId,
                                   TaskStatusDto taskStatusDto, TaskPriorityDto taskPriorityDto)
            throws ForbiddenException {
        Task task = taskRepository.findByIdNotDeleted(taskId).orElseThrow(
                () -> new EntityNotFoundException("No active task with id " + taskId));
        Long thisTaskProjectId = task.getProject().getId();

        if (projectAuthorityUtil.hasManagerialAuthority(
                thisTaskProjectId, authenticatedUser.getId())) {
            updatePresentField(authenticatedUser, task, updateTaskDto,
                    taskStatusDto, taskPriorityDto);
            return taskMapper.toTaskDto(taskRepository.save(task));
        } else {
            throw new ForbiddenException(NO_ACCESS_PERMISSION_FOR_TASK);
        }
    }

    @Override
    public void deleteTask(Long authenticatedUserId, Long taskId) throws ForbiddenException {
        Task task = taskRepository.findByIdNotDeleted(taskId).orElseThrow(
                () -> new EntityNotFoundException("No active task with id " + taskId));
        Long thisTaskProjectId = task.getProject().getId();

        if (projectAuthorityUtil.hasManagerialAuthority(thisTaskProjectId, authenticatedUserId)) {
            commentRepository.deleteAllByTaskId(taskId);
            taskRepository.deleteById(taskId);
        } else {
            throw new ForbiddenException(NO_PERMISSION_FOR_TASK_DELETION);
        }
    }

    @Override
    public List<TaskResponse> getTasksWithLabel(
            Long authenticatedUserId, Long labelId, Pageable pageable) {
        if (labelRepository.existsByIdAndUserId(labelId, authenticatedUserId)) {
            return taskMapper.toTaskDtoList(
                    taskRepository.findAllByLabelIdNonDeleted(labelId, pageable).getContent());
        } else {
            throw new EntityNotFoundException(
                    "No label with id " + labelId + " for user with id " + authenticatedUserId);
        }
    }

    private void updatePresentField(User authenticatedUser,
                                    Task task,
                                    UpdateTaskRequest updateTaskDto,
                                    TaskStatusDto taskStatusDto,
                                    TaskPriorityDto taskPriorityDto) throws ForbiddenException {
        if (areStringsValid(updateTaskDto.name(), task.getName())) {
            task.setName(updateTaskDto.name());
        }
        if (areStringsValid(updateTaskDto.description(), task.getDescription())) {
            task.setDescription(updateTaskDto.description());
        }
        if (areDatesValid(updateTaskDto.dueDate(), task.getDueDate())) {
            task.setDueDate(updateTaskDto.dueDate());
        }

        if (updateTaskDto.projectId() != null) {
            Project project = projectRepository.findByIdNotDeleted(updateTaskDto.projectId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "No project with id " + updateTaskDto.projectId()));

            if (projectAuthorityUtil.hasManagerialAuthority(
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
