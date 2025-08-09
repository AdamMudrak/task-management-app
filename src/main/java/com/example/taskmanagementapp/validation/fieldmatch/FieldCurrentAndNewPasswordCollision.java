package com.example.taskmanagementapp.validation.fieldmatch;

import static com.example.taskmanagementapp.constant.validation.ValidationConstants.PASSWORD_COLLISION;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Constraint(validatedBy = FieldCurrentAndNewPasswordCollisionValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldCurrentAndNewPasswordCollision {
    String message() default PASSWORD_COLLISION;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
