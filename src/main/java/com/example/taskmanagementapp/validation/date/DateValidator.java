package com.example.taskmanagementapp.validation.date;

import static com.example.taskmanagementapp.constant.validation.ValidationConstants.COMPILED_DATE_PATTERN;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class DateValidator implements ConstraintValidator<Date, LocalDate> {
    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext constraintValidatorContext) {
        return date == null || COMPILED_DATE_PATTERN.matcher(date.toString()).matches();
    }
}
