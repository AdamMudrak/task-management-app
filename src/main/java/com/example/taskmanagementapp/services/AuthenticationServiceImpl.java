package com.example.taskmanagementapp.services;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCESS;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.CONFIRMATION;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.PASSWORD_SET_SUCCESSFULLY;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.RANDOM_PASSWORD_REQUIRED_CHARS;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.RANDOM_PASSWORD_STRENGTH;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.REFRESH;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.REFRESH_TOKEN;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.REGISTERED_BUT_NOT_ACTIVATED;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.RESET;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.SUCCESS_EMAIL;

import com.example.taskmanagementapp.constants.security.SecurityConstants;
import com.example.taskmanagementapp.dtos.authentication.request.GetLinkToResetPasswordDto;
import com.example.taskmanagementapp.dtos.authentication.request.SetNewPasswordDto;
import com.example.taskmanagementapp.dtos.authentication.request.UserLoginRequestDto;
import com.example.taskmanagementapp.dtos.authentication.response.AccessTokenDto;
import com.example.taskmanagementapp.dtos.authentication.response.LinkToResetPasswordSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.SendLinkToResetPasswordDto;
import com.example.taskmanagementapp.dtos.authentication.response.UserLoginResponseDto;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.entities.tokens.ParamToken;
import com.example.taskmanagementapp.exceptions.conflictexpections.PasswordMismatch;
import com.example.taskmanagementapp.exceptions.forbidden.LoginException;
import com.example.taskmanagementapp.exceptions.gone.LinkExpiredException;
import com.example.taskmanagementapp.exceptions.notfoundexceptions.EntityNotFoundException;
import com.example.taskmanagementapp.repositories.user.UserRepository;
import com.example.taskmanagementapp.repositories.paramtoken.ParamTokenRepository;
import com.example.taskmanagementapp.security.email.PasswordEmailService;
import com.example.taskmanagementapp.security.jwtutils.abstr.JwtAbstractUtil;
import com.example.taskmanagementapp.security.jwtutils.strategy.JwtStrategy;
import com.example.taskmanagementapp.security.utils.RandomStringUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtStrategy jwtStrategy;
    private final PasswordEmailService passwordEmailService;
    private final RandomStringUtil randomStringUtil;
    private final ParamTokenRepository paramTokenRepository;

    @Override
    public UserLoginResponseDto authenticateUser(UserLoginRequestDto requestDto) {
        if (requestDto.emailOrUsername().contains("@")) {
            return authenticateEmail(requestDto);
        } else {
            return authenticateUsername(requestDto);
        }
    }

    @Override
    public SendLinkToResetPasswordDto initiatePasswordReset(String emailOrUsername) {
        User currentUser;
        if (emailOrUsername.contains("@")) {
            currentUser = isCreatedByEmail(emailOrUsername);
        } else {
            currentUser = isCreatedByUsername(emailOrUsername);
        }
        isEnabled(currentUser);
        passwordEmailService.sendActionMessage(currentUser.getEmail(), RESET);
        return new SendLinkToResetPasswordDto(SUCCESS_EMAIL);
    }

    @Override
    public LinkToResetPasswordSuccessDto confirmResetPassword(String token) {
        JwtAbstractUtil jwtAbstractUtil = jwtStrategy.getStrategy(ACCESS);
        try {
            jwtAbstractUtil.isValidToken(token);
        } catch (JwtException e) {
            throw new LinkExpiredException("This link is expired. Please, submit another "
                    + " \"forgot password\" request");
        }
        String email = getEmailFromTokenSecure(token, jwtAbstractUtil);
        String randomPassword = randomStringUtil.generateRandomString(RANDOM_PASSWORD_STRENGTH)
                + RANDOM_PASSWORD_REQUIRED_CHARS;
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new EntityNotFoundException("User with email " + email + " was not found"));
        user.setPassword(passwordEncoder.encode(randomPassword));
        userRepository.save(user);
        passwordEmailService.sendResetPassword(email, randomPassword);
        return new LinkToResetPasswordSuccessDto("SUCCESS!!!");//TODO
    }

    @Override
    public GetLinkToResetPasswordDto changePassword(HttpServletRequest httpServletRequest,
                                                SetNewPasswordDto userSetNewPasswordRequestDto) {
        String token = parseToken(httpServletRequest);
        JwtAbstractUtil jwtAbstractUtil = jwtStrategy.getStrategy(ACCESS);
        String email = jwtAbstractUtil.getUsername(token);
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new EntityNotFoundException("User with email " + email + " was not found"));
        if (!isCurrentPasswordValid(user, userSetNewPasswordRequestDto)) {
            throw new PasswordMismatch("Wrong password. Try resetting "
                    + "password and using a new random password");
        }
        user.setPassword(passwordEncoder
                .encode(userSetNewPasswordRequestDto.newPassword()));
        userRepository.save(user);
        return new GetLinkToResetPasswordDto(PASSWORD_SET_SUCCESSFULLY);
    }

    @Override
    public AccessTokenDto refreshToken(HttpServletRequest httpServletRequest) {
        Cookie cookie = findRefreshCookie(httpServletRequest);
        JwtAbstractUtil refreshUtil = jwtStrategy.getStrategy(REFRESH);
        JwtAbstractUtil accessUtil = jwtStrategy.getStrategy(ACCESS);
        String refreshToken = cookie.getValue();
        if (refreshUtil.isValidToken(refreshToken)) {
            String username = refreshUtil.getUsername(refreshToken);
            return new AccessTokenDto(accessUtil.generateToken(username));
        }
        throw new LoginException("Something went wrong with your access");
    }

    private UserLoginResponseDto authenticateEmail(UserLoginRequestDto requestDto) {
        User currentUser = isCreatedByEmail(requestDto.emailOrUsername());
        isEnabled(currentUser);
        return getTokens(currentUser.getUsername(), requestDto.password());
    }

    private UserLoginResponseDto authenticateUsername(UserLoginRequestDto requestDto) {
        User currentUser = isCreatedByUsername(requestDto.emailOrUsername());
        isEnabled(currentUser);
        return getTokens(currentUser.getUsername(), requestDto.password());
    }

    private boolean isCurrentPasswordValid(User user,
                                           SetNewPasswordDto userSetNewPasswordRequestDto) {
        return passwordEncoder
                .matches(userSetNewPasswordRequestDto.currentPassword(), user.getPassword());
    }

    private User isCreatedByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new LoginException(
                        "Either login" + " or password is invalid"));
    }

    private User isCreatedByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Either login" + " or password is invalid"));
    }

    private void isEnabled(User user) {
        if (!user.isEnabled()) {
            passwordEmailService.sendActionMessage(user.getUsername(), CONFIRMATION);
            throw new LoginException(REGISTERED_BUT_NOT_ACTIVATED);
        }
    }

    private String getEmailFromTokenSecure(String token, JwtAbstractUtil jwtAbstractUtil) {
        ParamToken paramToken = paramTokenRepository.findByActionToken(token).orElseThrow(()
                -> new EntityNotFoundException(
                "No such request was found... The link might be expired or forged"));
        String email = jwtAbstractUtil.getUsername(paramToken.getActionToken());
        paramTokenRepository.deleteById(paramToken.getId());
        return email;
    }

    private Cookie findRefreshCookie(HttpServletRequest httpServletRequest) {
        return Arrays.stream(httpServletRequest.getCookies())
                .filter(refreshCookie -> refreshCookie.getName().equals(REFRESH_TOKEN))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Couldn't find RT in cookies"));
    }

    private String parseToken(HttpServletRequest httpServletRequest) {
        String bearerToken = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken
                .startsWith(SecurityConstants.BEARER)) {
            bearerToken = bearerToken.substring(SecurityConstants.BEGIN_INDEX);
        }
        return bearerToken;
    }

    private UserLoginResponseDto getTokens(String email, String password) {
        final Authentication authentication;
        try {
            authentication = authenticationManager
                    .authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    email, password));

        } catch (AuthenticationException authenticationException) {
            throw new LoginException("Either login or password is invalid");
        }
        JwtAbstractUtil jwtAbstractUtil = jwtStrategy.getStrategy(ACCESS);
        String accessToken = jwtAbstractUtil.generateToken(authentication.getName());
        jwtAbstractUtil = jwtStrategy.getStrategy(REFRESH);
        String refreshToken = jwtAbstractUtil.generateToken(authentication.getName());
        return new UserLoginResponseDto(accessToken, refreshToken);
    }
}
