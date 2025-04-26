package com.example.taskmanagementapp.services;

import com.example.taskmanagementapp.dtos.authentication.request.SetNewPasswordDto;
import com.example.taskmanagementapp.dtos.authentication.request.UserLoginRequestDto;
import com.example.taskmanagementapp.dtos.authentication.request.UserRegistrationRequestDto;
import com.example.taskmanagementapp.dtos.authentication.response.ChangePasswordSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.LinkToResetPasswordSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.LoginSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.RegistrationConfirmationSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.RegistrationSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.SendLinkToResetPasswordDto;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.badrequest.RegistrationException;
import com.example.taskmanagementapp.exceptions.conflictexpections.PasswordMismatchException;
import com.example.taskmanagementapp.exceptions.forbidden.LoginException;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {
    LoginSuccessDto authenticateUser(UserLoginRequestDto requestDto,
                                     HttpServletResponse httpServletResponse) throws LoginException;

    SendLinkToResetPasswordDto sendPasswordResetLink(String emailOrUsername) throws LoginException;

    LinkToResetPasswordSuccessDto confirmResetPassword(String token);

    ChangePasswordSuccessDto changePassword(User user,
                                            SetNewPasswordDto userSetNewPasswordRequestDto)
                                                                throws PasswordMismatchException;

    RegistrationSuccessDto register(UserRegistrationRequestDto requestDto)
                                                                    throws RegistrationException;

    RegistrationConfirmationSuccessDto confirmRegistration(String token);
}
