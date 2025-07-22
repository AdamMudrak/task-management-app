package com.example.taskmanagementapp.services.impl;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCOUNT_IS_LOCKED;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.CHECK_YOUR_EMAIL;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.DIVIDER;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.LOGIN_SUCCESS;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.PASSWORD_SET_SUCCESSFULLY;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.RANDOM_PASSWORD_REQUIRED_CHARS;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.RANDOM_PASSWORD_STRENGTH;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.REGISTERED;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.REGISTERED_BUT_NOT_ACTIVATED;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.REGISTRATION_CONFIRMED;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.SEND_LINK_TO_RESET_PASSWORD;
import static com.example.taskmanagementapp.constants.validation.ValidationConstants.COMPILED_EMAIL_PATTERN;

import com.example.taskmanagementapp.dtos.authentication.TokenBearerDto;
import com.example.taskmanagementapp.dtos.authentication.request.LoginRequest;
import com.example.taskmanagementapp.dtos.authentication.request.PasswordChangeRequest;
import com.example.taskmanagementapp.dtos.authentication.request.RegistrationRequest;
import com.example.taskmanagementapp.dtos.authentication.response.LoginResponse;
import com.example.taskmanagementapp.dtos.authentication.response.PasswordChangeResponse;
import com.example.taskmanagementapp.dtos.authentication.response.PasswordResetLinkResponse;
import com.example.taskmanagementapp.dtos.authentication.response.RegistrationConfirmationResponse;
import com.example.taskmanagementapp.dtos.authentication.response.RegistrationResponse;
import com.example.taskmanagementapp.dtos.authentication.response.ResetLinkSentResponse;
import com.example.taskmanagementapp.entities.Role;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.EntityNotFoundException;
import com.example.taskmanagementapp.exceptions.LinkExpiredException;
import com.example.taskmanagementapp.exceptions.LoginException;
import com.example.taskmanagementapp.exceptions.PasswordMismatchException;
import com.example.taskmanagementapp.exceptions.RegistrationException;
import com.example.taskmanagementapp.mappers.UserMapper;
import com.example.taskmanagementapp.repositories.RoleRepository;
import com.example.taskmanagementapp.repositories.UserRepository;
import com.example.taskmanagementapp.security.jwtutils.abstr.JwtAbstractUtil;
import com.example.taskmanagementapp.security.jwtutils.strategy.JwtStrategy;
import com.example.taskmanagementapp.security.jwtutils.strategy.JwtType;
import com.example.taskmanagementapp.services.AuthenticationService;
import com.example.taskmanagementapp.services.email.PasswordEmailService;
import com.example.taskmanagementapp.services.email.RegisterConfirmEmailService;
import com.example.taskmanagementapp.services.utils.ParamFromHttpRequestUtil;
import com.example.taskmanagementapp.services.utils.RandomStringUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtStrategy jwtStrategy;
    private final PasswordEmailService passwordEmailService;
    private final RegisterConfirmEmailService registerConfirmEmailService;
    private final ParamFromHttpRequestUtil paramFromHttpRequestUtil;
    private final RoleRepository roleRepository;
    private final Long accessExpiration;
    private final Long refreshExpiration;

    public AuthenticationServiceImpl(UserRepository userRepository,
                                     UserMapper userMapper,
                                     AuthenticationManager authenticationManager,
                                     PasswordEncoder passwordEncoder,
                                     JwtStrategy jwtStrategy,
                                     PasswordEmailService passwordEmailService,
                                     RegisterConfirmEmailService registerConfirmEmailService,
                                     ParamFromHttpRequestUtil paramFromHttpRequestUtil,
                                     RoleRepository roleRepository,
                                     @Value("${jwt.access.expiration}") Long accessExpiration,
                                     @Value("${jwt.refresh.expiration}") Long refreshExpiration) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtStrategy = jwtStrategy;
        this.passwordEmailService = passwordEmailService;
        this.registerConfirmEmailService = registerConfirmEmailService;
        this.paramFromHttpRequestUtil = paramFromHttpRequestUtil;
        this.roleRepository = roleRepository;
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    @Override
    public LoginResponse authenticateUser(LoginRequest requestDto,
                                          HttpServletResponse httpServletResponse)
                                                                            throws LoginException {
        TokenBearerDto tokenBearer;
        if (COMPILED_EMAIL_PATTERN.matcher(requestDto.emailOrUsername()).matches()) {
            tokenBearer = authenticateEmail(requestDto);
        } else {
            tokenBearer = authenticateUsername(requestDto);
        }
        return addTokensToCookies(tokenBearer, httpServletResponse);
    }

    @Override
    public PasswordResetLinkResponse sendPasswordResetLink(String emailOrUsername)
            throws LoginException {
        User currentUser;
        if (COMPILED_EMAIL_PATTERN.matcher(emailOrUsername).matches()) {
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
        passwordEmailService.sendInitiatePasswordReset(currentUser.getEmail());
        return new PasswordResetLinkResponse(SEND_LINK_TO_RESET_PASSWORD);
    }

    @Override
    public ResetLinkSentResponse confirmResetPassword(HttpServletRequest request) {
        String token = paramFromHttpRequestUtil.parseRandomParameterAndToken(request);

        JwtAbstractUtil jwtAbstractUtil = jwtStrategy.getStrategy(JwtType.ACTION);
        try {
            jwtAbstractUtil.isValidToken(token);
        } catch (JwtException e) {
            throw new LinkExpiredException("This link is expired. Please, submit another "
                    + " \"forgot password\" request");
            /*This message doesn't represent the real problem to
            hide the usage of JWT from the client.*/
        }
        String email = jwtAbstractUtil.getUsername(token);
        String randomPassword = RandomStringUtil.generateRandomString(RANDOM_PASSWORD_STRENGTH)
                + RANDOM_PASSWORD_REQUIRED_CHARS;
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new EntityNotFoundException("User with email " + email + " was not found"));
        user.setPassword(passwordEncoder.encode(randomPassword));
        userRepository.save(user);
        passwordEmailService.sendResetPassword(email, randomPassword);
        return new ResetLinkSentResponse(CHECK_YOUR_EMAIL);
    }

    @Override
    public PasswordChangeResponse changePassword(User user,
                                                 PasswordChangeRequest userSetNewPasswordRequestDto)
            throws PasswordMismatchException {
        if (!isCurrentPasswordValid(user, userSetNewPasswordRequestDto)) {
            throw new PasswordMismatchException("Wrong password. Try resetting "
                    + "password and using a new random password");
        }
        user.setPassword(passwordEncoder
                .encode(userSetNewPasswordRequestDto.newPassword()));
        userRepository.save(user);
        return new PasswordChangeResponse(PASSWORD_SET_SUCCESSFULLY);
    }

    @Override
    public RegistrationResponse register(RegistrationRequest requestDto)
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
        registerConfirmEmailService.sendRegisterConfirmEmail(user.getEmail());
        return new RegistrationResponse(REGISTERED);
    }

    @Override
    public RegistrationConfirmationResponse confirmRegistration(HttpServletRequest request) {
        String token = paramFromHttpRequestUtil.parseRandomParameterAndToken(request);
        JwtAbstractUtil jwtAbstractUtil = jwtStrategy.getStrategy(JwtType.ACTION);
        String email = jwtAbstractUtil.getUsername(token);
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("User with email "
                        + email + " was not found"));
        user.setEnabled(true);
        userRepository.save(user);
        return new RegistrationConfirmationResponse(REGISTRATION_CONFIRMED);
    }

    private TokenBearerDto authenticateEmail(LoginRequest requestDto) throws LoginException {
        User currentUser = getIfExistsByEmail(requestDto.emailOrUsername());
        isEnabled(currentUser);
        return getTokens(currentUser.getUsername(), requestDto.password());
    }

    private TokenBearerDto authenticateUsername(LoginRequest requestDto)
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
                                           PasswordChangeRequest userSetNewPasswordRequestDto) {
        return passwordEncoder
                .matches(userSetNewPasswordRequestDto.currentPassword(), user.getPassword());
    }

    private User getIfExistsByEmail(String email) throws LoginException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new LoginException(
                        "Either login" + " or password is invalid"));
    }

    private User getIfExistsByUsername(String username) throws LoginException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new LoginException(
                        "Either login" + " or password is invalid"));
    }

    private void isEnabled(User user) throws LoginException {
        if (!user.isAccountNonLocked()) {
            throw new LoginException(ACCOUNT_IS_LOCKED);
        }
        if (!user.isEnabled()) {
            registerConfirmEmailService.sendRegisterConfirmEmail(user.getEmail());
            throw new LoginException(REGISTERED_BUT_NOT_ACTIVATED);
        }
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
        JwtAbstractUtil jwtAbstractUtil = jwtStrategy.getStrategy(JwtType.ACCESS);
        String accessToken = jwtAbstractUtil.generateToken(authentication.getName());
        jwtAbstractUtil = jwtStrategy.getStrategy(JwtType.REFRESHMENT);
        String refreshToken = jwtAbstractUtil.generateToken(authentication.getName());
        return new TokenBearerDto(accessToken, refreshToken);
    }

    private LoginResponse addTokensToCookies(TokenBearerDto tokenBearerDto,
                                             HttpServletResponse httpServletResponse) {
        String accessCookie = "accessToken" + "=" + tokenBearerDto.accessToken()
                + "; Path=/"
                + "; HttpOnly"
                + "; Secure"
                + "; SameSite=Strict"
                + "; Max-Age=" + accessExpiration / DIVIDER;
        httpServletResponse.addHeader("Set-Cookie", accessCookie);

        String refreshCookie = "refreshToken" + "=" + tokenBearerDto.refreshToken()
                + "; Path=/"
                + "; HttpOnly"
                + "; Secure"
                + "; SameSite=Strict"
                + "; Max-Age=" + refreshExpiration / DIVIDER;
        httpServletResponse.addHeader("Set-Cookie", refreshCookie);
        return new LoginResponse(LOGIN_SUCCESS);
    }
}
