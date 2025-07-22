package com.example.taskmanagementapp.services;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.LOGIN_SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.taskmanagementapp.dtos.authentication.request.LoginRequest;
import com.example.taskmanagementapp.dtos.authentication.response.LoginResponse;
import com.example.taskmanagementapp.entities.Role;
import com.example.taskmanagementapp.entities.User;
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
import com.example.taskmanagementapp.services.impl.AuthenticationServiceImpl;
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
        void givenLockedUser_whenAuthenticateUserByEmail_thenThrowLoginException()
                throws LoginException {
            //when
            when(userRepository.findByEmail(Constants.EMAIL_3))
                    .thenReturn(Optional.of(disabledUser));

            //then
            LoginRequest lockedUserLoginRequest =
                    new LoginRequest(Constants.EMAIL_3, Constants.PASSWORD_1);
            HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
            assertThrows(LoginException.class, () -> authenticationService.authenticateUser(
                    lockedUserLoginRequest, httpServletResponse));
        }

        @Test
        void givenInvalidPassword_whenAuthenticateUserByEmail_thenThrowLoginException()
                throws LoginException {
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
        void givenInvalidPassword_whenAuthenticateUserByUsername_thenThrowLoginException()
                throws LoginException {
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
}
