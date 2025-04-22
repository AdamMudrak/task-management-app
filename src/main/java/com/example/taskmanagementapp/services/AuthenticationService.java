package com.example.taskmanagementapp.services;

import com.example.taskmanagementapp.dtos.authentication.request.SetNewPasswordDto;
import com.example.taskmanagementapp.dtos.authentication.request.UserLoginRequestDto;
import com.example.taskmanagementapp.dtos.authentication.request.UserRegistrationRequestDto;
import com.example.taskmanagementapp.dtos.authentication.response.ChangePasswordSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.LinkToResetPasswordSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.LoginSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.RegistrationConfirmationSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.SendLinkToResetPasswordDto;
import com.example.taskmanagementapp.dtos.authentication.response.UserRegistrationResponseDto;
import com.example.taskmanagementapp.entities.User;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {
    LoginSuccessDto authenticateUser(UserLoginRequestDto requestDto,
                                     HttpServletResponse httpServletResponse);

    SendLinkToResetPasswordDto sendPasswordResetLink(String emailOrUsername);

    LinkToResetPasswordSuccessDto confirmResetPassword(String token);

    ChangePasswordSuccessDto changePassword(User user,
                                            SetNewPasswordDto userSetNewPasswordRequestDto);

    UserRegistrationResponseDto register(UserRegistrationRequestDto requestDto);

    RegistrationConfirmationSuccessDto confirmRegistration(String token);
}
