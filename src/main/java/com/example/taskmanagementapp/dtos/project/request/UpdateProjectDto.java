package com.example.taskmanagementapp.dtos.project.request;

import com.example.taskmanagementapp.constants.dtos.ProjectDtoConstants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdateProjectDto(@Schema(name = ProjectDtoConstants.NAME,
                                        example = ProjectDtoConstants.NAME_EXAMPLE)
                               String name,
                               @Schema(name = ProjectDtoConstants.DESCRIPTION,
                                       example = ProjectDtoConstants.DESCRIPTION_EXAMPLE)
                               String description,
                               @Schema(name = ProjectDtoConstants.START_DATE,
                                       example = ProjectDtoConstants.START_DATE_EXAMPLE)
                               LocalDate startDate,
                               @Schema(name = ProjectDtoConstants.END_DATE,
                                       example = ProjectDtoConstants.END_DATE_EXAMPLE)
                               LocalDate endDate,
                               Long ownerId) {}
