package com.example.taskmanagementapp.validation.fieldmatch;

import com.example.taskmanagementapp.dtos.authentication.request.UserRegistrationRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FieldRegisterMatchValidator implements ConstraintValidator<FieldRegisterMatch,
        UserRegistrationRequestDto> {
    @Override
    public boolean isValid(UserRegistrationRequestDto userRegistrationRequestDto,
                           ConstraintValidatorContext constraintValidatorContext) {
        if (userRegistrationRequestDto.password() == null
                || userRegistrationRequestDto.repeatPassword() == null) {
            return false;
        }
        return userRegistrationRequestDto.password()
                .equals(userRegistrationRequestDto.repeatPassword());
    }
}
