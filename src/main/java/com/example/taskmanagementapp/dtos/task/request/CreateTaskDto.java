package com.example.taskmanagementapp.dtos.task.request;

import java.time.LocalDate;

public record CreateTaskDto(
        String name,
        String description,
        LocalDate dueDate,
        Long projectId,
        Long assigneeId){}
