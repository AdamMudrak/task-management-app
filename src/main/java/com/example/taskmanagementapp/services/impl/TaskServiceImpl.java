package com.example.taskmanagementapp.services.impl;

import static com.example.taskmanagementapp.services.utils.UpdateValueValidatorUtil.areDatesValid;
import static com.example.taskmanagementapp.services.utils.UpdateValueValidatorUtil.areStringsValid;

import com.example.taskmanagementapp.dtos.task.request.TaskPriorityDto;
import com.example.taskmanagementapp.dtos.task.request.TaskRequest;
import com.example.taskmanagementapp.dtos.task.request.TaskStatusDto;
import com.example.taskmanagementapp.dtos.task.request.UpdateTaskRequest;
import com.example.taskmanagementapp.dtos.task.response.TaskResponse;
import com.example.taskmanagementapp.entities.Project;
import com.example.taskmanagementapp.entities.Task;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.EntityNotFoundException;
import com.example.taskmanagementapp.exceptions.ForbiddenException;
import com.example.taskmanagementapp.mappers.TaskMapper;
import com.example.taskmanagementapp.repositories.CommentRepository;
import com.example.taskmanagementapp.repositories.LabelRepository;
import com.example.taskmanagementapp.repositories.ProjectRepository;
import com.example.taskmanagementapp.repositories.TaskRepository;
import com.example.taskmanagementapp.repositories.UserRepository;
import com.example.taskmanagementapp.services.TaskService;
import com.example.taskmanagementapp.services.email.TaskAssignmentEmailService;
import com.example.taskmanagementapp.services.utils.ProjectAuthorityUtil;
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
            throw new ForbiddenException("You have no permission to access this project tasks");
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
            throw new ForbiddenException("You have no permission to access this task");
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
            throw new ForbiddenException("You have no permission to update this task");
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
            throw new ForbiddenException("You have no permission to delete this task");
        }
    }

    @Override
    public List<TaskResponse> getTasksWithLabel(User user, Long labelId, Pageable pageable) {
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
            Project project = projectRepository.findById(updateTaskDto.projectId()).orElseThrow(
                    () -> new EntityNotFoundException(
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
