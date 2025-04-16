package com.example.taskmanagementapp.services;

import com.example.taskmanagementapp.dtos.authentication.request.SetNewPasswordDto;
import com.example.taskmanagementapp.dtos.authentication.request.UserLoginRequestDto;
import com.example.taskmanagementapp.dtos.authentication.request.UserRegistrationRequestDto;
import com.example.taskmanagementapp.dtos.authentication.response.AccessTokenDto;
import com.example.taskmanagementapp.dtos.authentication.response.ChangePasswordSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.LinkToResetPasswordSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.RegistrationConfirmationSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.SendLinkToResetPasswordDto;
import com.example.taskmanagementapp.dtos.authentication.response.UserLoginResponseDto;
import com.example.taskmanagementapp.dtos.authentication.response.UserRegistrationResponseDto;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationService {
    UserLoginResponseDto authenticateUser(UserLoginRequestDto requestDto);

    SendLinkToResetPasswordDto sendPasswordResetLink(String emailOrUsername);

    LinkToResetPasswordSuccessDto confirmResetPassword(String token);

    ChangePasswordSuccessDto changePassword(HttpServletRequest httpServletRequest,
                                            SetNewPasswordDto userSetNewPasswordRequestDto);

    AccessTokenDto refreshToken(HttpServletRequest httpServletRequest);

    UserRegistrationResponseDto register(UserRegistrationRequestDto requestDto);

    RegistrationConfirmationSuccessDto confirmRegistration(String email);
}
