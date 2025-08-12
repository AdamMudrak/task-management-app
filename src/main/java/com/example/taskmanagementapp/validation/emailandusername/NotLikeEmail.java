package com.example.taskmanagementapp.validation.emailandusername;

import static com.example.taskmanagementapp.constant.validation.ValidationConstants.INVALID_USERNAME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Constraint(validatedBy = NotLikeEmailValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NotLikeEmail {
    String message() default INVALID_USERNAME;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
