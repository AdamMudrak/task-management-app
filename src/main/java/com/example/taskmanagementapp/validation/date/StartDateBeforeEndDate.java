package com.example.taskmanagementapp.validation.date;

import static com.example.taskmanagementapp.constants.validation.ValidationConstants.END_DATE_EARLIER_THAN_START_DATE;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Constraint(validatedBy = StartDateBeforeEndDateValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface StartDateBeforeEndDate {
    String message() default END_DATE_EARLIER_THAN_START_DATE;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
