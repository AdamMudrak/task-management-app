package com.example.taskmanagementapp.service;

import com.example.taskmanagementapp.dto.authentication.request.LoginRequest;
import com.example.taskmanagementapp.dto.authentication.request.PasswordChangeRequest;
import com.example.taskmanagementapp.dto.authentication.request.RegistrationRequest;
import com.example.taskmanagementapp.dto.authentication.response.LoginResponse;
import com.example.taskmanagementapp.dto.authentication.response.PasswordChangeResponse;
import com.example.taskmanagementapp.dto.authentication.response.PasswordResetLinkResponse;
import com.example.taskmanagementapp.dto.authentication.response.RegistrationConfirmationResponse;
import com.example.taskmanagementapp.dto.authentication.response.RegistrationResponse;
import com.example.taskmanagementapp.dto.authentication.response.ResetLinkSentResponse;
import com.example.taskmanagementapp.entity.User;
import com.example.taskmanagementapp.exception.LoginException;
import com.example.taskmanagementapp.exception.PasswordMismatchException;
import com.example.taskmanagementapp.exception.RegistrationException;
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
