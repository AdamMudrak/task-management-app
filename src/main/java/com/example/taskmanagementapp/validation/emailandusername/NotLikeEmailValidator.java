package com.example.taskmanagementapp.validation.emailandusername;

import static com.example.taskmanagementapp.constants.validation.ValidationConstants.PATTERN_OF_EMAIL;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class NotLikeEmailValidator implements ConstraintValidator<NotLikeEmail, String> {
    private static final Pattern emailPattern = Pattern.compile(PATTERN_OF_EMAIL);

    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        return email != null && emailPattern.matcher(email).matches();
    }
}
