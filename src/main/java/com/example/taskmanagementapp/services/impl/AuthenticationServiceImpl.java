package com.example.taskmanagementapp.services.impl;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCESS;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCESS_TOKEN;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACTION;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.CHECK_YOUR_EMAIL;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.CONFIRMATION;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.DIVIDER;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.JWT_ACCESS_EXPIRATION;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.JWT_REFRESH_EXPIRATION;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.LOGIN_SUCCESS;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.PASSWORD_SET_SUCCESSFULLY;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.RANDOM_PASSWORD_REQUIRED_CHARS;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.RANDOM_PASSWORD_STRENGTH;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.REFRESH;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.REFRESH_TOKEN;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.REGISTERED;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.REGISTERED_BUT_NOT_ACTIVATED;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.REGISTRATION_CONFIRMED;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.RESET;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.SEND_LINK_TO_RESET_PASSWORD;
import static com.example.taskmanagementapp.constants.validation.ValidationConstants.COMPILED_PATTERN;

import com.example.taskmanagementapp.dtos.authentication.TokenBearerDto;
import com.example.taskmanagementapp.dtos.authentication.request.SetNewPasswordDto;
import com.example.taskmanagementapp.dtos.authentication.request.UserLoginRequestDto;
import com.example.taskmanagementapp.dtos.authentication.request.UserRegistrationRequestDto;
import com.example.taskmanagementapp.dtos.authentication.response.ChangePasswordSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.LinkToResetPasswordSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.LoginSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.RegistrationConfirmationSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.RegistrationSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.SendLinkToResetPasswordDto;
import com.example.taskmanagementapp.entities.ParamToken;
import com.example.taskmanagementapp.entities.Role;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.badrequest.RegistrationException;
import com.example.taskmanagementapp.exceptions.conflictexpections.PasswordMismatchException;
import com.example.taskmanagementapp.exceptions.forbidden.LoginException;
import com.example.taskmanagementapp.exceptions.gone.LinkExpiredException;
import com.example.taskmanagementapp.exceptions.notfoundexceptions.EntityNotFoundException;
import com.example.taskmanagementapp.mappers.UserMapper;
import com.example.taskmanagementapp.repositories.paramtoken.ParamTokenRepository;
import com.example.taskmanagementapp.repositories.role.RoleRepository;
import com.example.taskmanagementapp.repositories.user.UserRepository;
import com.example.taskmanagementapp.security.jwtutils.abstr.JwtAbstractUtil;
import com.example.taskmanagementapp.security.jwtutils.strategy.JwtStrategy;
import com.example.taskmanagementapp.services.AuthenticationService;
import com.example.taskmanagementapp.services.email.PasswordEmailService;
import com.example.taskmanagementapp.services.utils.RandomStringUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtStrategy jwtStrategy;
    private final PasswordEmailService passwordEmailService;
    private final RandomStringUtil randomStringUtil;
    private final ParamTokenRepository paramTokenRepository;
    private final RoleRepository roleRepository;
    @Value(JWT_ACCESS_EXPIRATION)
    private Long accessExpiration;
    @Value(JWT_REFRESH_EXPIRATION)
    private Long refreshExpiration;

    @Override
    public LoginSuccessDto authenticateUser(UserLoginRequestDto requestDto,
                                            HttpServletResponse httpServletResponse)
                                                                            throws LoginException {
        TokenBearerDto tokenBearer;
        if (COMPILED_PATTERN.matcher(requestDto.emailOrUsername()).matches()) {
            tokenBearer = authenticateEmail(requestDto);
        } else {
            tokenBearer = authenticateUsername(requestDto);
        }
        return addTokensToCookies(tokenBearer, httpServletResponse);
    }

    @Override
    public SendLinkToResetPasswordDto sendPasswordResetLink(String emailOrUsername)
            throws LoginException {
        User currentUser;
        if (COMPILED_PATTERN.matcher(emailOrUsername).matches()) {
            currentUser = userRepository.findByEmail(emailOrUsername)
                    .orElseThrow(
                            () -> new EntityNotFoundException("No user with email "
                                    + emailOrUsername + " found"));
        } else {
            currentUser = userRepository.findByUsername(emailOrUsername)
                    .orElseThrow(
                            () -> new EntityNotFoundException("No user with username "
                                    + emailOrUsername + " found"));
        }
        isEnabled(currentUser);
        passwordEmailService.sendActionMessage(currentUser.getEmail(), RESET);
        return new SendLinkToResetPasswordDto(SEND_LINK_TO_RESET_PASSWORD);
    }

    @Override
    public LinkToResetPasswordSuccessDto confirmResetPassword(String token) {
        JwtAbstractUtil jwtAbstractUtil = jwtStrategy.getStrategy(ACTION);
        try {
            jwtAbstractUtil.isValidToken(token);
        } catch (JwtException e) {
            throw new LinkExpiredException("This link is expired. Please, submit another "
                    + " \"forgot password\" request");
            /*This message doesn't represent the real problem to
            hide the usage of JWT from the client.*/
        }
        String email = getEmailFromTokenSecure(token, jwtAbstractUtil);
        String randomPassword = randomStringUtil.generateRandomString(RANDOM_PASSWORD_STRENGTH)
                + RANDOM_PASSWORD_REQUIRED_CHARS;
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new EntityNotFoundException("User with email " + email + " was not found"));
        user.setPassword(passwordEncoder.encode(randomPassword));
        userRepository.save(user);
        passwordEmailService.sendResetPassword(email, randomPassword);
        return new LinkToResetPasswordSuccessDto(CHECK_YOUR_EMAIL);
    }

    @Override
    public ChangePasswordSuccessDto changePassword(User user,
                                                   SetNewPasswordDto userSetNewPasswordRequestDto)
            throws PasswordMismatchException {
        if (!isCurrentPasswordValid(user, userSetNewPasswordRequestDto)) {
            throw new PasswordMismatchException("Wrong password. Try resetting "
                    + "password and using a new random password");
        }
        user.setPassword(passwordEncoder
                .encode(userSetNewPasswordRequestDto.newPassword()));
        userRepository.save(user);
        return new ChangePasswordSuccessDto(PASSWORD_SET_SUCCESSFULLY);
    }

    @Transactional
    @Override
    public RegistrationSuccessDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.existsByUsername(requestDto.username())) {
            throw new RegistrationException("User with username "
                    + requestDto.username() + " already exists");
        }

        if (userRepository.existsByEmail(requestDto.email())) {
            throw new RegistrationException("User with email "
                    + requestDto.email() + " already exists");
        }

        User user = userMapper.toUser(requestDto);
        user.setPassword(passwordEncoder.encode(requestDto.password()));
        assignBasicRole(user);
        userRepository.save(user);
        passwordEmailService.sendActionMessage(user.getEmail(), CONFIRMATION);
        return new RegistrationSuccessDto(REGISTERED);
    }

    @Transactional
    @Override
    public RegistrationConfirmationSuccessDto confirmRegistration(String token) {
        JwtAbstractUtil jwtAbstractUtil = jwtStrategy.getStrategy(ACTION);
        String email = jwtAbstractUtil.getUsername(token);
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("User with email "
                        + email + " was not found"));
        user.setEnabled(true);
        userRepository.save(user);
        ParamToken paramToken = paramTokenRepository.findByActionToken(token).orElseThrow(()
                -> new EntityNotFoundException("No such request"));
        paramTokenRepository.deleteById(paramToken.getId());
        return new RegistrationConfirmationSuccessDto(REGISTRATION_CONFIRMED);
    }

    private TokenBearerDto authenticateEmail(UserLoginRequestDto requestDto) throws LoginException {
        User currentUser = getIfExistsByEmail(requestDto.emailOrUsername());
        isEnabled(currentUser);
        return getTokens(currentUser.getUsername(), requestDto.password());
    }

    private TokenBearerDto authenticateUsername(UserLoginRequestDto requestDto)
            throws LoginException {
        User currentUser = getIfExistsByUsername(requestDto.emailOrUsername());
        isEnabled(currentUser);
        return getTokens(currentUser.getUsername(), requestDto.password());
    }

    private void assignBasicRole(User user) {
        Role basicRole = roleRepository.findByName(Role.RoleName.ROLE_USER);
        user.setRole(basicRole);
    }

    private boolean isCurrentPasswordValid(User user,
                                           SetNewPasswordDto userSetNewPasswordRequestDto) {
        return passwordEncoder
                .matches(userSetNewPasswordRequestDto.currentPassword(), user.getPassword());
    }

    private User getIfExistsByEmail(String email) throws LoginException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new LoginException(
                        "Either login" + " or password is invalid"));
    }

    private User getIfExistsByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Either login" + " or password is invalid"));
    }

    private void isEnabled(User user) throws LoginException {
        if (!user.isAccountNonLocked()) {
            throw new LoginException("Your account is locked. Consider contacting support team");
        }
        if (!user.isEnabled()) {
            passwordEmailService.sendActionMessage(user.getEmail(), CONFIRMATION);
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

    private TokenBearerDto getTokens(String email, String password) throws LoginException {
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
        return new TokenBearerDto(accessToken, refreshToken);
    }

    private LoginSuccessDto addTokensToCookies(TokenBearerDto tokenBearerDto,
                                                    HttpServletResponse httpServletResponse) {
        String accessCookie = ACCESS_TOKEN + "=" + tokenBearerDto.accessToken()
                + "; Path=/"
                + "; HttpOnly"
                + "; Secure"
                + "; SameSite=Strict"
                + "; Max-Age=" + accessExpiration / DIVIDER;
        httpServletResponse.addHeader("Set-Cookie", accessCookie);

        String refreshCookie = REFRESH_TOKEN + "=" + tokenBearerDto.refreshToken()
                + "; Path=/"
                + "; HttpOnly"
                + "; Secure"
                + "; SameSite=Strict"
                + "; Max-Age=" + refreshExpiration / DIVIDER;
        httpServletResponse.addHeader("Set-Cookie", refreshCookie);
        return new LoginSuccessDto(LOGIN_SUCCESS);
    }
}
