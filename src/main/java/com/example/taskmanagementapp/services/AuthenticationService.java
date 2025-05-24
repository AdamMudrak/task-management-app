package com.example.taskmanagementapp.services;

import com.example.taskmanagementapp.dtos.authentication.request.LoginRequest;
import com.example.taskmanagementapp.dtos.authentication.request.PasswordChangeRequest;
import com.example.taskmanagementapp.dtos.authentication.request.RegistrationRequest;
import com.example.taskmanagementapp.dtos.authentication.response.LoginResponse;
import com.example.taskmanagementapp.dtos.authentication.response.PasswordChangeResponse;
import com.example.taskmanagementapp.dtos.authentication.response.PasswordResetLinkResponse;
import com.example.taskmanagementapp.dtos.authentication.response.RegistrationConfirmationResponse;
import com.example.taskmanagementapp.dtos.authentication.response.RegistrationResponse;
import com.example.taskmanagementapp.dtos.authentication.response.ResetLinkSentResponse;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.LoginException;
import com.example.taskmanagementapp.exceptions.PasswordMismatchException;
import com.example.taskmanagementapp.exceptions.RegistrationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {
    LoginResponse authenticateUser(LoginRequest requestDto,
                                   HttpServletResponse httpServletResponse) throws LoginException;

    PasswordResetLinkResponse sendPasswordResetLink(String emailOrUsername) throws LoginException;

    ResetLinkSentResponse confirmResetPassword(HttpServletRequest request);

    PasswordChangeResponse changePassword(User user,
                                          PasswordChangeRequest userSetNewPasswordRequestDto)
                                                                throws PasswordMismatchException;

    RegistrationResponse register(RegistrationRequest requestDto)
                                                                    throws RegistrationException;

    RegistrationConfirmationResponse confirmRegistration(HttpServletRequest request);
}
