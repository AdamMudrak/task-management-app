package com.example.taskmanagementapp.validation.fieldmatch;

import static com.example.taskmanagementapp.constant.validation.ValidationConstants.NEW_PASSWORD_MISMATCH;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = FieldSetNewPasswordMatchValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldSetNewPasswordMatch {
    String message() default NEW_PASSWORD_MISMATCH;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
