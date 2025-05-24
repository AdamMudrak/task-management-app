package com.example.taskmanagementapp.dtos.project.request;

import com.example.taskmanagementapp.validation.date.Date;
import com.example.taskmanagementapp.validation.date.StartDateBeforeEndDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@StartDateBeforeEndDate
public record UpdateProjectRequest(@Schema(name = "name",
                                        example = "Best Project")
                               String name,
                                   @Schema(name = "description",
                                       example = "Project for best company")
                               String description,
                                   @Schema(name = "startDate",
                                       example = "2025-01-01")
                               @Date
                               LocalDate startDate,
                                   @Schema(name = "endDate",
                                       example = "2025-12-31")
                               @Date
                               LocalDate endDate,
                                   @Positive
                               Long ownerId) {}
