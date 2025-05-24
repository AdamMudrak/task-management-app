package com.example.taskmanagementapp.validation.password;

import static com.example.taskmanagementapp.constants.validation.ValidationConstants.COMPILED_PASSWORD_PATTERN;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<Password, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        return password != null && COMPILED_PASSWORD_PATTERN.matcher(password).matches();
    }
}
