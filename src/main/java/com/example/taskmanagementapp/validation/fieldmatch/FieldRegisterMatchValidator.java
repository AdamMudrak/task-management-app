package com.example.taskmanagementapp.validation.fieldmatch;

import com.example.taskmanagementapp.dto.authentication.request.RegistrationRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FieldRegisterMatchValidator implements ConstraintValidator<FieldRegisterMatch,
        RegistrationRequest> {
    @Override
    public boolean isValid(RegistrationRequest userRegistrationRequestDto,
                           ConstraintValidatorContext constraintValidatorContext) {
        if (userRegistrationRequestDto.password() == null
                || userRegistrationRequestDto.repeatPassword() == null) {
            return false;
        }
        return userRegistrationRequestDto.password()
                .equals(userRegistrationRequestDto.repeatPassword());
    }
}
