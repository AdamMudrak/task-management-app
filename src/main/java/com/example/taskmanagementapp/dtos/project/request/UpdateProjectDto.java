package com.example.taskmanagementapp.dtos.project.request;

import com.example.taskmanagementapp.constants.dtos.ProjectDtoConstants;
import com.example.taskmanagementapp.validation.date.Date;
import com.example.taskmanagementapp.validation.date.StartDateBeforeEndDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@StartDateBeforeEndDate
public record UpdateProjectDto(@Schema(name = ProjectDtoConstants.NAME,
                                        example = ProjectDtoConstants.NAME_EXAMPLE)
                               String name,
                               @Schema(name = ProjectDtoConstants.DESCRIPTION,
                                       example = ProjectDtoConstants.DESCRIPTION_EXAMPLE)
                               String description,
                               @Schema(name = ProjectDtoConstants.START_DATE,
                                       example = ProjectDtoConstants.START_DATE_EXAMPLE)
                               @Date
                               LocalDate startDate,
                               @Schema(name = ProjectDtoConstants.END_DATE,
                                       example = ProjectDtoConstants.END_DATE_EXAMPLE)
                               @Date
                               LocalDate endDate,
                               Long ownerId) {}
