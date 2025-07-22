package com.example.taskmanagementapp.services.impl;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCOUNT_IS_LOCKED;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.LOGIN_SUCCESS;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.REGISTERED_BUT_NOT_ACTIVATED;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.SEND_LINK_TO_RESET_PASSWORD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.taskmanagementapp.dtos.authentication.request.LoginRequest;
import com.example.taskmanagementapp.dtos.authentication.response.LoginResponse;
import com.example.taskmanagementapp.dtos.authentication.response.PasswordResetLinkResponse;
import com.example.taskmanagementapp.entities.Role;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.EntityNotFoundException;
import com.example.taskmanagementapp.exceptions.LoginException;
import com.example.taskmanagementapp.mappers.UserMapper;
import com.example.taskmanagementapp.repositories.RoleRepository;
import com.example.taskmanagementapp.repositories.UserRepository;
import com.example.taskmanagementapp.security.jwtutils.impl.JwtAccessUtil;
import com.example.taskmanagementapp.security.jwtutils.impl.JwtRefreshUtil;
import com.example.taskmanagementapp.security.jwtutils.strategy.JwtStrategy;
import com.example.taskmanagementapp.security.jwtutils.strategy.JwtType;
import com.example.taskmanagementapp.services.email.PasswordEmailService;
import com.example.taskmanagementapp.services.email.RegisterConfirmEmailService;
import com.example.taskmanagementapp.services.utils.ParamFromHttpRequestUtil;
import com.example.taskmanagementapp.testutils.Constants;
import com.example.taskmanagementapp.testutils.ObjectFactory;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceImplTest {
    private static final Role role = ObjectFactory.getUserRole();
    private static final User user = ObjectFactory.getUser1(role);
    private static final User disabledUser = ObjectFactory.getDisabledUser(role);
    private static final User notActiveUser = ObjectFactory.getNotActiveUser(role);
    private static final List<GrantedAuthority> grantedAuthorities = List.of(
            new SimpleGrantedAuthority(role.getName().name()));

    private UserRepository userRepository;
    private UserMapper userMapper;
    private AuthenticationManager authenticationManager;
    private PasswordEncoder passwordEncoder;
    private JwtStrategy jwtStrategy;
    private PasswordEmailService passwordEmailService;
    private RegisterConfirmEmailService registerConfirmEmailService;
    private ParamFromHttpRequestUtil paramFromHttpRequestUtil;
    private RoleRepository roleRepository;
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userMapper = mock(UserMapper.class);
        authenticationManager = mock(AuthenticationManager.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtStrategy = mock(JwtStrategy.class);
        passwordEmailService = mock(PasswordEmailService.class);
        registerConfirmEmailService = mock(RegisterConfirmEmailService.class);
        paramFromHttpRequestUtil = mock(ParamFromHttpRequestUtil.class);
        roleRepository = mock(RoleRepository.class);
        authenticationService = new AuthenticationServiceImpl(
                userRepository,
                userMapper,
                authenticationManager,
                passwordEncoder,
                jwtStrategy,
                passwordEmailService,
                registerConfirmEmailService,
                paramFromHttpRequestUtil,
                roleRepository,
                600000L,
                604800000L);
    }

    @Nested
    class AuthenticateUser {
        @Test
        void givenValidLoginRequestDto_whenAuthenticateUserByEmail_thenSuccessfullyLogin()
                throws LoginException {
            //when
            when(userRepository.findByEmail(Constants.EMAIL_1)).thenReturn(Optional.of(user));
            when(authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            Constants.USERNAME_1, Constants.PASSWORD_1)))
                    .thenReturn(new UsernamePasswordAuthenticationToken(
                            Constants.USERNAME_1, Constants.PASSWORD_1_DB, grantedAuthorities));

            JwtAccessUtil jwtAccessUtil = mock(JwtAccessUtil.class);
            when(jwtStrategy.getStrategy(JwtType.ACCESS))
                    .thenReturn(jwtAccessUtil);

            JwtRefreshUtil jwtRefreshUtil = mock(JwtRefreshUtil.class);
            when(jwtStrategy.getStrategy(JwtType.REFRESHMENT))
                    .thenReturn(jwtRefreshUtil);

            //then
            LoginRequest validLoginRequest =
                    new LoginRequest(Constants.EMAIL_1, Constants.PASSWORD_1);
            HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
            LoginResponse loginResponseResult = authenticationService.authenticateUser(
                    validLoginRequest, httpServletResponse);
            assertEquals(new LoginResponse(LOGIN_SUCCESS), loginResponseResult);
        }

        @Test
        void givenNotExistingEmail_whenAuthenticateUserByEmail_thenThrowEntityNotFound() {
            //when
            when(userRepository.findByEmail(Constants.EMAIL_5))
                    .thenThrow(new EntityNotFoundException(
                            "No user with email " + Constants.EMAIL_5 + " found"));

            //then
            LoginRequest invalidLoginRequest =
                    new LoginRequest(Constants.EMAIL_5, Constants.PASSWORD_1);
            HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
            EntityNotFoundException userNotFoundException =
                    assertThrows(EntityNotFoundException.class, () ->
                            authenticationService
                                    .authenticateUser(invalidLoginRequest, httpServletResponse));
            assertEquals("No user with email "
                    + Constants.EMAIL_5 + " found", userNotFoundException.getMessage());
        }

        @Test
        void givenNotExistingUsername_whenAuthenticateUserByUsername_thenThrowEntityNotFound() {
            //when
            when(userRepository.findByUsername(Constants.USERNAME_5)).thenThrow(
                    new EntityNotFoundException("No user with username "
                    + Constants.USERNAME_5 + " found"));

            //then
            LoginRequest invalidLoginRequest =
                    new LoginRequest(Constants.USERNAME_5, Constants.PASSWORD_1);
            HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
            EntityNotFoundException userNotFoundException =
                    assertThrows(EntityNotFoundException.class, () ->
                            authenticationService
                                    .authenticateUser(invalidLoginRequest, httpServletResponse));
            assertEquals("No user with username "
                    + Constants.USERNAME_5 + " found", userNotFoundException.getMessage());
        }

        @Test
        void givenValidLoginRequestDto_whenAuthenticateUserByUsername_thenSuccessfullyLogin()
                throws LoginException {
            //given
            Role role = ObjectFactory.getUserRole();
            User user = ObjectFactory.getUser1(role);
            List<GrantedAuthority> grantedAuthorities = List.of(
                    new SimpleGrantedAuthority(role.getName().name()));

            //when
            when(userRepository.findByUsername(Constants.USERNAME_1)).thenReturn(Optional.of(user));
            when(authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            Constants.USERNAME_1, Constants.PASSWORD_1)))
                    .thenReturn(new UsernamePasswordAuthenticationToken(
                            Constants.USERNAME_1, Constants.PASSWORD_1_DB, grantedAuthorities));

            JwtAccessUtil jwtAccessUtil = mock(JwtAccessUtil.class);
            when(jwtStrategy.getStrategy(JwtType.ACCESS))
                    .thenReturn(jwtAccessUtil);

            JwtRefreshUtil jwtRefreshUtil = mock(JwtRefreshUtil.class);
            when(jwtStrategy.getStrategy(JwtType.REFRESHMENT))
                    .thenReturn(jwtRefreshUtil);

            //then
            LoginRequest validLoginRequest =
                    new LoginRequest(Constants.USERNAME_1, Constants.PASSWORD_1);
            HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
            LoginResponse loginResponseResult = authenticationService.authenticateUser(
                    validLoginRequest, httpServletResponse);
            assertEquals(new LoginResponse(LOGIN_SUCCESS), loginResponseResult);
        }

        @Test
        void givenLockedUser_whenAuthenticateUserByEmail_thenThrowLoginException() {
            //when
            when(userRepository.findByEmail(Constants.EMAIL_3))
                    .thenReturn(Optional.of(disabledUser));

            //then
            LoginRequest lockedUserLoginRequest =
                    new LoginRequest(Constants.EMAIL_3, Constants.PASSWORD_1);
            HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
            LoginException loginException = assertThrows(LoginException.class,
                    () -> authenticationService.authenticateUser(
                    lockedUserLoginRequest, httpServletResponse));

            assertEquals(ACCOUNT_IS_LOCKED, loginException.getMessage());
        }

        @Test
        void givenNonActivatedUser_whenAuthenticateUserByEmail_thenThrowLoginException() {
            //when
            when(userRepository.findByEmail(Constants.EMAIL_4))
                    .thenReturn(Optional.of(notActiveUser));

            //then
            LoginRequest notActiveUserRequest =
                    new LoginRequest(Constants.EMAIL_4, Constants.PASSWORD_1);
            HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
            LoginException loginException = assertThrows(LoginException.class,
                    () -> authenticationService.authenticateUser(
                            notActiveUserRequest, httpServletResponse));

            assertEquals(REGISTERED_BUT_NOT_ACTIVATED, loginException.getMessage());
        }

        @Test
        void givenInvalidPassword_whenAuthenticateUserByEmail_thenThrowLoginException() {
            //given
            Role role = ObjectFactory.getUserRole();
            User user = ObjectFactory.getUser1(role);

            //when
            when(userRepository.findByEmail(Constants.EMAIL_1)).thenReturn(Optional.of(user));
            when(authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            Constants.USERNAME_1, Constants.PASSWORD_2)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            //then
            LoginRequest invalidLoginRequest =
                    new LoginRequest(Constants.EMAIL_1, Constants.PASSWORD_2);
            HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);

            assertThrows(LoginException.class, () -> authenticationService.authenticateUser(
                    invalidLoginRequest, httpServletResponse));
        }

        @Test
        void givenInvalidPassword_whenAuthenticateUserByUsername_thenThrowLoginException() {
            //given
            Role role = ObjectFactory.getUserRole();
            User user = ObjectFactory.getUser1(role);

            //when
            when(userRepository.findByUsername(Constants.USERNAME_1)).thenReturn(Optional.of(user));
            when(authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            Constants.USERNAME_1, Constants.PASSWORD_2)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            //then
            LoginRequest invalidLoginRequest =
                    new LoginRequest(Constants.USERNAME_1, Constants.PASSWORD_2);
            HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);

            assertThrows(LoginException.class, () -> authenticationService.authenticateUser(
                    invalidLoginRequest, httpServletResponse));
        }
    }

    @Nested
    class SendPasswordResetLink {
        @Test
        void givenAnEmailOfEnabledUser_whenSendPasswordResetLink_thenSuccessfullySendLink()
                throws LoginException {
            when(userRepository.findByEmail(Constants.EMAIL_1)).thenReturn(Optional.of(user));
            assertEquals(new PasswordResetLinkResponse(SEND_LINK_TO_RESET_PASSWORD),
                    authenticationService.sendPasswordResetLink(Constants.EMAIL_1));
        }

        @Test
        void givenAUsernameOfEnabledUser_whenSendPasswordResetLink_thenSuccessfullySendLink()
                throws LoginException {
            when(userRepository.findByUsername(Constants.USERNAME_1)).thenReturn(Optional.of(user));
            assertEquals(new PasswordResetLinkResponse(SEND_LINK_TO_RESET_PASSWORD),
                    authenticationService.sendPasswordResetLink(Constants.USERNAME_1));
        }

        @Test
        void givenAnEmailOfDisabledUser_whenSendPasswordResetLink_thenThrowLoginException() {
            //when
            when(userRepository.findByEmail(Constants.EMAIL_3))
                    .thenReturn(Optional.of(disabledUser));

            //then
            LoginException loginException = assertThrows(LoginException.class,
                    () -> authenticationService.sendPasswordResetLink(Constants.EMAIL_3));

            assertEquals(ACCOUNT_IS_LOCKED, loginException.getMessage());
        }

        @Test
        void givenAnEmailOfNonActiveUser_whenSendPasswordResetLink_thenThrowLoginException() {
            //when
            when(userRepository.findByEmail(Constants.EMAIL_4))
                    .thenReturn(Optional.of(notActiveUser));

            //then
            LoginException loginException = assertThrows(LoginException.class,
                    () -> authenticationService.sendPasswordResetLink(Constants.EMAIL_4));

            assertEquals(REGISTERED_BUT_NOT_ACTIVATED, loginException.getMessage());
        }

        @Test
        void givenAnEmailOfNonExistingUser_whenSendPasswordResetLink_thenThrowEntityNotFound() {
            //when
            when(userRepository.findByEmail(Constants.EMAIL_5))
                    .thenThrow(new EntityNotFoundException(
                            "No user with email " + Constants.EMAIL_5 + " found"));

            //then
            EntityNotFoundException userNotFoundException =
                    assertThrows(EntityNotFoundException.class, () ->
                            authenticationService
                                    .sendPasswordResetLink(Constants.EMAIL_5));
            assertEquals("No user with email "
                    + Constants.EMAIL_5 + " found", userNotFoundException.getMessage());
        }

        @Test
        void givenAUsernameOfNonExistingUser_whenSendPasswordResetLink_thenThrowEntityNotFound() {
            //when
            when(userRepository.findByUsername(Constants.USERNAME_5))
                    .thenThrow(new EntityNotFoundException(
                            "No user with username " + Constants.USERNAME_5 + " found"));

            //then
            EntityNotFoundException userNotFoundException =
                    assertThrows(EntityNotFoundException.class, () ->
                            authenticationService
                                    .sendPasswordResetLink(Constants.USERNAME_5));
            assertEquals("No user with username "
                    + Constants.USERNAME_5 + " found", userNotFoundException.getMessage());
        }
    }
}
