package com.example.taskmanagementapp.validation.password;

import static com.example.taskmanagementapp.constants.validation.ValidationConstants.PASSWORD_PATTERN;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<Password, String> {
    private static final Pattern passwordPattern = Pattern.compile(PASSWORD_PATTERN);

    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        return password != null && passwordPattern.matcher(password).matches();
    }
}
