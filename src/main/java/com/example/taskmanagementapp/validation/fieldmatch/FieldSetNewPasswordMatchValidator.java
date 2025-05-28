package com.example.taskmanagementapp.validation.fieldmatch;

import com.example.taskmanagementapp.dtos.authentication.request.PasswordChangeRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FieldSetNewPasswordMatchValidator implements ConstraintValidator<
        FieldSetNewPasswordMatch, PasswordChangeRequest> {
    @Override
    public boolean isValid(PasswordChangeRequest userSetNewPasswordRequestDto,
                           ConstraintValidatorContext constraintValidatorContext) {
        if (userSetNewPasswordRequestDto.newPassword() == null
                || userSetNewPasswordRequestDto.repeatNewPassword() == null) {
            return false;
        }
        return userSetNewPasswordRequestDto.newPassword()
                .equals(userSetNewPasswordRequestDto.repeatNewPassword());
    }
}
