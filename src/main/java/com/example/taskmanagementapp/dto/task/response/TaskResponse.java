package com.example.taskmanagementapp.dto.task.response;

import com.example.taskmanagementapp.dto.task.request.TaskPriorityDto;
import com.example.taskmanagementapp.dto.task.request.TaskStatusDto;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TaskResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDate dueDate;
    private Long projectId;
    private Long assigneeId;
    private TaskStatusDto status;
    private TaskPriorityDto priority;
}
