package com.example.taskmanagementapp.validation.fieldmatch;

import com.example.budgetingapp.dtos.users.request.SetNewPasswordDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FieldCurrentAndNewPasswordCollisionValidator implements ConstraintValidator<
        FieldCurrentAndNewPasswordCollision, SetNewPasswordDto> {
    @Override
    public boolean isValid(SetNewPasswordDto userSetNewPasswordRequestDto,
                           ConstraintValidatorContext constraintValidatorContext) {
        if (userSetNewPasswordRequestDto.currentPassword() == null
                || userSetNewPasswordRequestDto.newPassword() == null) {
            return false;
        }
        return !userSetNewPasswordRequestDto.currentPassword()
                .equals(userSetNewPasswordRequestDto.newPassword());
    }
}
