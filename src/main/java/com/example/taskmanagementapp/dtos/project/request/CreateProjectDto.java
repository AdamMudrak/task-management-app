package com.example.taskmanagementapp.dtos.project.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.example.taskmanagementapp.constants.dtos.ProjectDtoConstants;
import com.example.taskmanagementapp.validation.date.Date;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateProjectDto(@Schema(name = ProjectDtoConstants.NAME,
                                        example = ProjectDtoConstants.NAME_EXAMPLE,
                                        requiredMode = REQUIRED)
                               @NotBlank
                               String name,
                               @Schema(name = ProjectDtoConstants.DESCRIPTION,
                                       example = ProjectDtoConstants.DESCRIPTION_EXAMPLE)
                               String description,
                               @Schema(name = ProjectDtoConstants.START_DATE,
                                       example = ProjectDtoConstants.START_DATE_EXAMPLE,
                                       requiredMode = REQUIRED)
                               @NotBlank
                               @Date
                               LocalDate startDate,
                               @Schema(name = ProjectDtoConstants.END_DATE,
                                       example = ProjectDtoConstants.END_DATE_EXAMPLE,
                                       requiredMode = REQUIRED)
                               @NotBlank
                               @Date
                               LocalDate endDate) {}
