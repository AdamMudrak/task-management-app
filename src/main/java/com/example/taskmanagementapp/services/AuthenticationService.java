package com.example.taskmanagementapp.services;

import com.example.taskmanagementapp.dtos.authentication.request.GetLinkToResetPasswordDto;
import com.example.taskmanagementapp.dtos.authentication.request.SetNewPasswordDto;
import com.example.taskmanagementapp.dtos.authentication.request.UserLoginRequestDto;
import com.example.taskmanagementapp.dtos.authentication.response.AccessTokenDto;
import com.example.taskmanagementapp.dtos.authentication.response.LinkToResetPasswordSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.SendLinkToResetPasswordDto;
import com.example.taskmanagementapp.dtos.authentication.response.UserLoginResponseDto;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationService {
    UserLoginResponseDto authenticateUser(UserLoginRequestDto requestDto);

    SendLinkToResetPasswordDto initiatePasswordReset(String emailOrUsername);

    LinkToResetPasswordSuccessDto confirmResetPassword(String token);

    GetLinkToResetPasswordDto changePassword(HttpServletRequest httpServletRequest,
                                             SetNewPasswordDto userSetNewPasswordRequestDto);

    AccessTokenDto refreshToken(HttpServletRequest httpServletRequest);
}
