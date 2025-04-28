package com.example.taskmanagementapp.dtos.task.request;

import java.time.LocalDate;

public record UpdateTaskDto(
        String name,
        String description,
        LocalDate dueDate,
        Long projectId,
        Long assigneeId) {
}
