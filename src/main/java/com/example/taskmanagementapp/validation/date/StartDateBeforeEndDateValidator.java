package com.example.taskmanagementapp.validation.date;

import com.example.taskmanagementapp.dto.project.request.UpdateProjectRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StartDateBeforeEndDateValidator
        implements ConstraintValidator<StartDateBeforeEndDate, UpdateProjectRequest> {
    @Override
    public boolean isValid(UpdateProjectRequest updateProjectDto,
                           ConstraintValidatorContext constraintValidatorContext) {
        return (updateProjectDto.startDate() == null || updateProjectDto.endDate() == null)
                || updateProjectDto.startDate().isBefore(updateProjectDto.endDate());
    }
}
