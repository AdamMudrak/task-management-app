package com.example.taskmanagementapp.dtos.project.response;

import com.example.taskmanagementapp.dtos.project.request.ProjectStatusDto;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private ProjectStatusDto statusDto;
    private Long ownerId;
    private Set<Long> employeeIds;
    private Set<Long> managerIds;
}
