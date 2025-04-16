package com.example.taskmanagementapp.controllers;

import static com.example.taskmanagementapp.constants.Constants.ACCESS_DENIED;
import static com.example.taskmanagementapp.constants.Constants.AUTHORIZATION_REQUIRED;
import static com.example.taskmanagementapp.constants.Constants.CODE_200;
import static com.example.taskmanagementapp.constants.Constants.CODE_201;
import static com.example.taskmanagementapp.constants.Constants.CODE_400;
import static com.example.taskmanagementapp.constants.Constants.CODE_403;
import static com.example.taskmanagementapp.constants.Constants.INVALID_ENTITY_VALUE;
import static com.example.taskmanagementapp.constants.Constants.ROLE_USER;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.AUTH;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.AUTH_API_DESCRIPTION;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.AUTH_API_NAME;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.CHANGE_PASSWORD;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.CHANGE_PASSWORD_SUMMARY;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.CONFIRM_REGISTRATION;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.CONFIRM_SUMMARY;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.EMAIL_LOGIN_SUMMARY;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.FORGOT_PASSWORD;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.INITIATE_PASSWORD_RESET_SUMMARY;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.LOGIN;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.REFRESH_ACCESS_TOKEN;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.REFRESH_ACCESS_TOKEN_SUMMARY;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.REGISTER;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.REGISTER_SUMMARY;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.RESET_PASSWORD;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.RESET_PASSWORD_SUMMARY;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.SUCCESSFULLY_CHANGE_PASSWORD;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.SUCCESSFULLY_CONFIRMED;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.SUCCESSFULLY_INITIATED_PASSWORD_RESET;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.SUCCESSFULLY_LOGGED_IN;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.SUCCESSFULLY_REFRESHED_TOKEN;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.SUCCESSFULLY_REGISTERED;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.SUCCESSFULLY_RESET_PASSWORD;

import com.example.taskmanagementapp.constants.Constants;
import com.example.taskmanagementapp.dtos.authentication.request.GetLinkToResetPasswordDto;
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
import com.example.taskmanagementapp.security.utils.RandomParamFromHttpRequestUtil;
import com.example.taskmanagementapp.services.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@Tag(name = AUTH_API_NAME,
        description = AUTH_API_DESCRIPTION)
@RequestMapping(AUTH)
public class AuthController {
    private final AuthenticationService authenticationService;
    private final RandomParamFromHttpRequestUtil randomParamFromHttpRequestUtil;

    @Operation(summary = REGISTER_SUMMARY)
    @ApiResponse(responseCode = CODE_201, description =
            SUCCESSFULLY_REGISTERED)
    @ApiResponse(responseCode = CODE_400, description = INVALID_ENTITY_VALUE)
    @PostMapping(REGISTER)
    @ResponseStatus(HttpStatus.CREATED)
    public UserRegistrationResponseDto register(
            @RequestBody @Valid UserRegistrationRequestDto requestDto) {
        return authenticationService.register(requestDto);
    }

    @Operation(summary = CONFIRM_SUMMARY, hidden = true)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_CONFIRMED)
    @ApiResponse(responseCode = CODE_403, description = ACCESS_DENIED)
    @GetMapping(CONFIRM_REGISTRATION)
    public RegistrationConfirmationSuccessDto confirmRegistration(
            HttpServletRequest httpServletRequest) {
        randomParamFromHttpRequestUtil.parseRandomParameterAndToken(httpServletRequest);
        return authenticationService
                    .confirmRegistration(randomParamFromHttpRequestUtil.getTokenFromRepo(
                            randomParamFromHttpRequestUtil.getRandomParameter(),
                            randomParamFromHttpRequestUtil.getToken()));
    }

    @Operation(summary = EMAIL_LOGIN_SUMMARY)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_LOGGED_IN)
    @ApiResponse(responseCode = CODE_400, description = INVALID_ENTITY_VALUE)
    @ApiResponse(responseCode = CODE_403, description = ACCESS_DENIED)
    @PostMapping(LOGIN)
    public UserLoginResponseDto login(@RequestBody @Valid UserLoginRequestDto request) {
        return authenticationService.authenticateUser(request);
    }

    @Operation(summary = INITIATE_PASSWORD_RESET_SUMMARY)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_INITIATED_PASSWORD_RESET)
    @ApiResponse(responseCode = CODE_400, description = INVALID_ENTITY_VALUE)
    @PostMapping(FORGOT_PASSWORD)
    public SendLinkToResetPasswordDto initiatePasswordReset(@Valid @RequestBody
                                                            GetLinkToResetPasswordDto request) {
        return authenticationService.sendPasswordResetLink(request.emailOrUsername());
    }

    @Operation(summary = RESET_PASSWORD_SUMMARY, hidden = true)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_RESET_PASSWORD)
    @ApiResponse(responseCode = CODE_400, description = INVALID_ENTITY_VALUE)
    @GetMapping(RESET_PASSWORD)
    public LinkToResetPasswordSuccessDto resetPassword(HttpServletRequest httpServletRequest) {
        randomParamFromHttpRequestUtil.parseRandomParameterAndToken(httpServletRequest);
        return authenticationService
                .confirmResetPassword(randomParamFromHttpRequestUtil.getTokenFromRepo(
                randomParamFromHttpRequestUtil.getRandomParameter(),
                randomParamFromHttpRequestUtil.getToken()));
    }

    @Operation(summary = CHANGE_PASSWORD_SUMMARY)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_CHANGE_PASSWORD)
    @ApiResponse(responseCode = CODE_400, description = INVALID_ENTITY_VALUE)
    @ApiResponse(responseCode = Constants.CODE_401, description = AUTHORIZATION_REQUIRED)
    @ApiResponse(responseCode = CODE_403, description = ACCESS_DENIED)
    @PreAuthorize(ROLE_USER)
    @PostMapping(CHANGE_PASSWORD)
    public ChangePasswordSuccessDto changePassword(HttpServletRequest httpServletRequest,
                                                   @RequestBody @Valid SetNewPasswordDto request) {
        return authenticationService.changePassword(httpServletRequest, request);
    }

    @Operation(summary = REFRESH_ACCESS_TOKEN_SUMMARY)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_REFRESHED_TOKEN)
    @ApiResponse(responseCode = CODE_403, description = ACCESS_DENIED)
    @PostMapping(REFRESH_ACCESS_TOKEN)
    public AccessTokenDto refreshToken(HttpServletRequest httpServletRequest) {
        return authenticationService.refreshToken(httpServletRequest);
    }
}
