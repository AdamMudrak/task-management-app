package com.example.taskmanagementapp.dtos.project.response;

import com.example.taskmanagementapp.dtos.project.request.ProjectStatusDto;
import java.time.LocalDateTime;
import java.util.Set;

public record ProjectDto(Long id,
                         String name,
                         String description,
                         LocalDateTime startDate,
                         LocalDateTime endDate,
                         ProjectStatusDto statusDto,
                         Long ownerId,
                         Set<Long> employeeIds) {}
