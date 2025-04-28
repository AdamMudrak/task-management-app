package com.example.taskmanagementapp.dtos.task.response;

import com.example.taskmanagementapp.dtos.task.request.TaskPriorityDto;
import com.example.taskmanagementapp.dtos.task.request.TaskStatusDto;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskDto {
    private String name;
    private String description;
    private LocalDate dueDate;
    private Long projectId;
    private Long assigneeId;
    private TaskStatusDto status;
    private TaskPriorityDto priority;
}
