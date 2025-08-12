package com.example.taskmanagementapp.dto.project.response;

import com.example.taskmanagementapp.dto.project.request.ProjectStatusDto;
import java.time.LocalDate;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public final class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProjectStatusDto statusDto;
    private Long ownerId;
    private Set<Long> employeeIds;
    private Set<Long> managerIds;
}
