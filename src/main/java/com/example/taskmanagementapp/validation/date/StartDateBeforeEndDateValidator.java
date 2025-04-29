package com.example.taskmanagementapp.validation.date;

import com.example.taskmanagementapp.dtos.project.request.UpdateProjectDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StartDateBeforeEndDateValidator
        implements ConstraintValidator<StartDateBeforeEndDate, UpdateProjectDto> {
    @Override
    public boolean isValid(UpdateProjectDto updateProjectDto,
                           ConstraintValidatorContext constraintValidatorContext) {
        return (updateProjectDto.startDate() == null || updateProjectDto.endDate() == null)
                || updateProjectDto.startDate().isBefore(updateProjectDto.endDate());
    }
}
