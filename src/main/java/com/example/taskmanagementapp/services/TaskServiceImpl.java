package com.example.taskmanagementapp.services;

import com.example.taskmanagementapp.dtos.task.request.CreateTaskDto;
import com.example.taskmanagementapp.dtos.task.request.TaskPriorityDto;
import com.example.taskmanagementapp.dtos.task.request.TaskStatusDto;
import com.example.taskmanagementapp.dtos.task.request.UpdateTaskDto;
import com.example.taskmanagementapp.dtos.task.response.TaskDto;
import com.example.taskmanagementapp.entities.Project;
import com.example.taskmanagementapp.entities.Role;
import com.example.taskmanagementapp.entities.Task;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.forbidden.ForbiddenException;
import com.example.taskmanagementapp.exceptions.notfoundexceptions.EntityNotFoundException;
import com.example.taskmanagementapp.mappers.TaskMapper;
import com.example.taskmanagementapp.repositories.project.ProjectRepository;
import com.example.taskmanagementapp.repositories.task.TaskRepository;
import com.example.taskmanagementapp.repositories.user.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    @Override
    public TaskDto createTask(User authenticatedUser,
                              CreateTaskDto createTaskDto,
                              TaskPriorityDto taskPriorityDto) throws ForbiddenException {
        Long projectId = createTaskDto.projectId();
        Long assigneeId = createTaskDto.assigneeId();

        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new EntityNotFoundException("No project with id " + projectId));
        if (project.isDeleted()) {
            throw new ForbiddenException("Project with id " + projectId + " is deleted");
        }
        if (!userRepository.existsById(assigneeId)) {
            throw new EntityNotFoundException("No user with id " + assigneeId);
        }
        if (isUserSupervisor(authenticatedUser)
                || isUserOwner(authenticatedUser, project)) {
            Task createTask = taskMapper.toCreateTask(createTaskDto);
            createTask.setStatus(Task.Status.NOT_STARTED);
            createTask.setPriority(Task.Priority.valueOf(taskPriorityDto.name()));
            return taskMapper.toTaskDto(taskRepository.save(createTask));
        } else {
            throw new ForbiddenException("You have no permission to modify project " + projectId);
        }
    }

    @Override
    public List<TaskDto> getTasksForProject(User authenticatedUser, Long projectId)
            throws ForbiddenException {
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new EntityNotFoundException("No project with id " + projectId));
        if (project.isDeleted()) {
            throw new ForbiddenException("Project with id " + projectId + " is deleted");
        }
        boolean hasAccess = isUserSupervisor(authenticatedUser)
                || isUserOwner(authenticatedUser, project)
                || isUserAssignee(authenticatedUser, project);

        if (hasAccess) {
            return taskMapper.toTaskDtoList(taskRepository.findAllByProjectIdNonDeleted(projectId));
        } else {
            throw new ForbiddenException("You have no permission to access this project tasks");
        }
    }

    @Override
    public TaskDto getTask(User authenticatedUser, Long taskId) throws ForbiddenException {
        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new EntityNotFoundException("No task with id " + taskId));
        if (task.isDeleted()) {
            throw new ForbiddenException("Task with id " + taskId + " is deleted");
        }
        if (isUserSupervisor(authenticatedUser)
                || isUserOwner(authenticatedUser, task.getProject())
                || isUserAssignee(authenticatedUser, task.getProject())) {
            return taskMapper.toTaskDto(task);
        } else {
            throw new ForbiddenException("You have no permission to access this task");
        }
    }

    @Override
    public TaskDto updateTask(User authenticatedUser, UpdateTaskDto updateTaskDto, Long taskId,
                              TaskStatusDto taskStatusDto, TaskPriorityDto taskPriorityDto)
            throws ForbiddenException {
        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new EntityNotFoundException("No task with id " + taskId));
        if (task.isDeleted()) {
            throw new ForbiddenException("Task with id " + taskId + " is deleted");
        }
        boolean hasAccess = isUserSupervisor(authenticatedUser)
                || isUserOwner(authenticatedUser, task.getProject());

        if (hasAccess) {
            updatePresentField(authenticatedUser, task, updateTaskDto,
                    taskStatusDto, taskPriorityDto);
            return taskMapper.toTaskDto(taskRepository.save(task));
        } else {
            throw new ForbiddenException("You have no permission to update this task");
        }
    }

    @Override
    public void deleteTask(User authenticatedUser, Long taskId) throws ForbiddenException {
        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new EntityNotFoundException("No task with id " + taskId));

        boolean hasAccess = isUserSupervisor(authenticatedUser)
                || isUserOwner(authenticatedUser, task.getProject());

        if (hasAccess) {
            taskRepository.deleteById(taskId);
        } else {
            throw new ForbiddenException("You have no permission to delete this task");
        }
    }

    private boolean isUserSupervisor(User user) {
        return user.getRole().getName().equals(Role.RoleName.ROLE_SUPERVISOR);
    }

    private boolean isUserOwner(User user, Project project) {
        return user.getId().equals(project.getOwner().getId());
    }

    private boolean isUserAssignee(User user, Project project) {
        return project.getEmployees().stream()
                .map(User::getId)
                .anyMatch(id -> user.getId().equals(id));
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

            if (isUserSupervisor(authenticatedUser) || isUserOwner(authenticatedUser, project)) {
                task.setProject(project);
            } else {
                throw new ForbiddenException(
                        "You have no permission to modify project " + project.getId());
            }
        }

        if (updateTaskDto.assigneeId() != null) {
            User assignee = userRepository.findById(updateTaskDto.assigneeId()).orElseThrow(
                    () -> new EntityNotFoundException(
                            "No user with id " + updateTaskDto.assigneeId()));

            if (!task.getProject().getEmployees().contains(assignee)) {
                throw new ForbiddenException("You can't assign employee " + assignee.getId()
                        + " to task " + task.getId()
                        + " since they are not in project " + task.getProject().getId());
            }
            task.setAssignee(assignee);
        }

        if (taskStatusDto != null) {
            task.setStatus(Task.Status.valueOf(taskStatusDto.name()));
        }

        if (taskPriorityDto != null) {
            task.setPriority(Task.Priority.valueOf(taskPriorityDto.name()));
        }
    }
}
