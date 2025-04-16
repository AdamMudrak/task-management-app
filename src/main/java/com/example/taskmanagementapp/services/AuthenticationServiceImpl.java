package com.example.taskmanagementapp.services;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCESS;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACTION;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.CONFIRMATION;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.PASSWORD_RESET_SUCCESSFULLY;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.PASSWORD_SET_SUCCESSFULLY;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.RANDOM_PASSWORD_REQUIRED_CHARS;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.RANDOM_PASSWORD_STRENGTH;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.REFRESH;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.REFRESH_TOKEN;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.REGISTERED;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.REGISTERED_BUT_NOT_ACTIVATED;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.REGISTRATION_CONFIRMED;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.RESET;
import static com.example.taskmanagementapp.constants.validation.ValidationConstants.PATTERN_OF_EMAIL;

import com.example.taskmanagementapp.constants.security.SecurityConstants;
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
import com.example.taskmanagementapp.entities.Role;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.entities.tokens.ParamToken;
import com.example.taskmanagementapp.exceptions.badrequest.RegistrationException;
import com.example.taskmanagementapp.exceptions.conflictexpections.PasswordMismatch;
import com.example.taskmanagementapp.exceptions.forbidden.LoginException;
import com.example.taskmanagementapp.exceptions.gone.LinkExpiredException;
import com.example.taskmanagementapp.exceptions.notfoundexceptions.EntityNotFoundException;
import com.example.taskmanagementapp.mappers.UserMapper;
import com.example.taskmanagementapp.repositories.paramtoken.ParamTokenRepository;
import com.example.taskmanagementapp.repositories.role.RoleRepository;
import com.example.taskmanagementapp.repositories.user.UserRepository;
import com.example.taskmanagementapp.security.email.PasswordEmailService;
import com.example.taskmanagementapp.security.jwtutils.abstr.JwtAbstractUtil;
import com.example.taskmanagementapp.security.jwtutils.strategy.JwtStrategy;
import com.example.taskmanagementapp.security.utils.RandomStringUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;
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
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtStrategy jwtStrategy;
    private final PasswordEmailService passwordEmailService;
    private final RandomStringUtil randomStringUtil;
    private final ParamTokenRepository paramTokenRepository;
    private final RoleRepository roleRepository;
    private final Pattern emailPattern = Pattern.compile(PATTERN_OF_EMAIL);

    @Override
    public UserLoginResponseDto authenticateUser(UserLoginRequestDto requestDto) {
        if (emailPattern.matcher(requestDto.emailOrUsername()).matches()) {
            return authenticateEmail(requestDto);
        } else {
            return authenticateUsername(requestDto);
        }
    }

    @Override
    public SendLinkToResetPasswordDto sendPasswordResetLink(String emailOrUsername) {
        User currentUser;
        if (emailPattern.matcher(emailOrUsername).matches()) {
            currentUser = getIfExistsByEmail(emailOrUsername);
        } else {
            currentUser = getIfExistsByUsername(emailOrUsername);
        }
        isEnabled(currentUser);
        passwordEmailService.sendActionMessage(currentUser.getEmail(), RESET);
        return new SendLinkToResetPasswordDto(PASSWORD_RESET_SUCCESSFULLY);
    }

    @Override
    public LinkToResetPasswordSuccessDto confirmResetPassword(String token) {
        JwtAbstractUtil jwtAbstractUtil = jwtStrategy.getStrategy(ACCESS);
        try {
            jwtAbstractUtil.isValidToken(token);
        } catch (JwtException e) {
            throw new LinkExpiredException("This link is expired. Please, submit another "
                    + " \"forgot password\" request");
            /*This message doesn't represent real problem to
            hide the fact of usage of JWT from the client.*/
        }
        String email = getEmailFromTokenSecure(token, jwtAbstractUtil);
        String randomPassword = randomStringUtil.generateRandomString(RANDOM_PASSWORD_STRENGTH)
                + RANDOM_PASSWORD_REQUIRED_CHARS;
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new EntityNotFoundException("User with email " + email + " was not found"));
        user.setPassword(passwordEncoder.encode(randomPassword));
        userRepository.save(user);
        passwordEmailService.sendResetPassword(email, randomPassword);
        return new LinkToResetPasswordSuccessDto(PASSWORD_RESET_SUCCESSFULLY);
    }

    @Override
    public ChangePasswordSuccessDto changePassword(HttpServletRequest httpServletRequest,
                                                   SetNewPasswordDto userSetNewPasswordRequestDto) {
        String token = parseToken(httpServletRequest);
        JwtAbstractUtil jwtAbstractUtil = jwtStrategy.getStrategy(ACCESS);
        String username = jwtAbstractUtil.getUsername(token);
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new EntityNotFoundException("User with username " + username + " was not found"));
        if (!isCurrentPasswordValid(user, userSetNewPasswordRequestDto)) {
            throw new PasswordMismatch("Wrong password. Try resetting "
                    + "password and using a new random password");
        }
        user.setPassword(passwordEncoder
                .encode(userSetNewPasswordRequestDto.newPassword()));
        userRepository.save(user);
        return new ChangePasswordSuccessDto(PASSWORD_SET_SUCCESSFULLY);
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

    @Transactional
    @Override
    public UserRegistrationResponseDto register(UserRegistrationRequestDto requestDto) {
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
        assignUserRole(user);
        userRepository.save(user);
        passwordEmailService.sendActionMessage(user.getEmail(), CONFIRMATION);
        return new UserRegistrationResponseDto(REGISTERED);
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

    private UserLoginResponseDto authenticateEmail(UserLoginRequestDto requestDto) {
        User currentUser = getIfExistsByEmail(requestDto.emailOrUsername());
        isEnabled(currentUser);
        return getTokens(currentUser.getUsername(), requestDto.password());
    }

    private UserLoginResponseDto authenticateUsername(UserLoginRequestDto requestDto) {
        User currentUser = getIfExistsByUsername(requestDto.emailOrUsername());
        isEnabled(currentUser);
        return getTokens(currentUser.getUsername(), requestDto.password());
    }

    private void assignUserRole(User user) {
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER);
        user.setRoles(Set.of(userRole));
    }

    private boolean isCurrentPasswordValid(User user,
                                           SetNewPasswordDto userSetNewPasswordRequestDto) {
        return passwordEncoder
                .matches(userSetNewPasswordRequestDto.currentPassword(), user.getPassword());
    }

    private User getIfExistsByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new LoginException(
                        "Either login" + " or password is invalid"));
    }

    private User getIfExistsByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Either login" + " or password is invalid"));
    }

    private void isEnabled(User user) {
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
