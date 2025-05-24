package com.example.taskmanagementapp.services;

import com.example.taskmanagementapp.dtos.authentication.request.PasswordChangeRequest;
import com.example.taskmanagementapp.dtos.authentication.request.UserLoginRequestDto;
import com.example.taskmanagementapp.dtos.authentication.request.UserRegistrationRequestDto;
import com.example.taskmanagementapp.dtos.authentication.response.ChangePasswordSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.LinkToResetPasswordSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.LoginSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.RegistrationConfirmationSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.RegistrationSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.SendLinkToResetPasswordDto;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.LoginException;
import com.example.taskmanagementapp.exceptions.PasswordMismatchException;
import com.example.taskmanagementapp.exceptions.RegistrationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {
    LoginSuccessDto authenticateUser(UserLoginRequestDto requestDto,
                                     HttpServletResponse httpServletResponse) throws LoginException;

    SendLinkToResetPasswordDto sendPasswordResetLink(String emailOrUsername) throws LoginException;

    LinkToResetPasswordSuccessDto confirmResetPassword(HttpServletRequest request);

    ChangePasswordSuccessDto changePassword(User user,
                                            PasswordChangeRequest userSetNewPasswordRequestDto)
                                                                throws PasswordMismatchException;

    RegistrationSuccessDto register(UserRegistrationRequestDto requestDto)
                                                                    throws RegistrationException;

    RegistrationConfirmationSuccessDto confirmRegistration(HttpServletRequest request);
}
