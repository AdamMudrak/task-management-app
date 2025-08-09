package com.example.taskmanagementapp.service;

import static com.example.taskmanagementapp.constant.security.SecurityConstants.ACCOUNT_IS_LOCKED;
import static com.example.taskmanagementapp.constant.security.SecurityConstants.CHECK_YOUR_EMAIL;
import static com.example.taskmanagementapp.constant.security.SecurityConstants.LINK_EXPIRED;
import static com.example.taskmanagementapp.constant.security.SecurityConstants.LOGIN_SUCCESS;
import static com.example.taskmanagementapp.constant.security.SecurityConstants.PASSWORD_MISMATCH;
import static com.example.taskmanagementapp.constant.security.SecurityConstants.PASSWORD_SET_SUCCESSFULLY;
import static com.example.taskmanagementapp.constant.security.SecurityConstants.REGISTERED;
import static com.example.taskmanagementapp.constant.security.SecurityConstants.REGISTERED_BUT_NOT_ACTIVATED;
import static com.example.taskmanagementapp.constant.security.SecurityConstants.REGISTRATION_CONFIRMED;
import static com.example.taskmanagementapp.constant.security.SecurityConstants.SEND_LINK_TO_RESET_PASSWORD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskmanagementapp.dto.authentication.request.LoginRequest;
import com.example.taskmanagementapp.dto.authentication.request.PasswordChangeRequest;
import com.example.taskmanagementapp.dto.authentication.request.RegistrationRequest;
import com.example.taskmanagementapp.dto.authentication.response.LoginResponse;
import com.example.taskmanagementapp.dto.authentication.response.PasswordChangeResponse;
import com.example.taskmanagementapp.dto.authentication.response.PasswordResetLinkResponse;
import com.example.taskmanagementapp.dto.authentication.response.RegistrationConfirmationResponse;
import com.example.taskmanagementapp.dto.authentication.response.RegistrationResponse;
import com.example.taskmanagementapp.dto.authentication.response.ResetLinkSentResponse;
import com.example.taskmanagementapp.entity.Role;
import com.example.taskmanagementapp.entity.User;
import com.example.taskmanagementapp.exception.EntityNotFoundException;
import com.example.taskmanagementapp.exception.LinkExpiredException;
import com.example.taskmanagementapp.exception.LoginException;
import com.example.taskmanagementapp.exception.PasswordMismatchException;
import com.example.taskmanagementapp.exception.RegistrationException;
import com.example.taskmanagementapp.mapper.UserMapper;
import com.example.taskmanagementapp.repository.RoleRepository;
import com.example.taskmanagementapp.repository.UserRepository;
import com.example.taskmanagementapp.security.jwtutil.abstr.JwtAbstractUtil;
import com.example.taskmanagementapp.security.jwtutil.impl.JwtAccessUtil;
import com.example.taskmanagementapp.security.jwtutil.impl.JwtActionUtil;
import com.example.taskmanagementapp.security.jwtutil.impl.JwtRefreshUtil;
import com.example.taskmanagementapp.security.jwtutil.strategy.JwtStrategy;
import com.example.taskmanagementapp.security.jwtutil.strategy.JwtType;
import com.example.taskmanagementapp.service.email.PasswordEmailService;
import com.example.taskmanagementapp.service.email.RegisterConfirmEmailService;
import com.example.taskmanagementapp.service.impl.AuthenticationServiceImpl;
import com.example.taskmanagementapp.service.utils.ParamFromHttpRequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceImplTest {
    private static final String USERNAME_1 = "JohnDoe";
    private static final String PASSWORD_1 = "Best_Password1@3$";
    private static final String PASSWORD_1_DB =
            "$2a$10$u4cOSEeePFyJlpvkPdtmhenMuPYhloQfrVS19DZU8/.5jtJNm7piW";
    private static final String EMAIL_1 = "john_doe@mail.com";
    private static final String PASSWORD_2 = "newPassword1@";
    private static final String PASSWORD_3 = "newPassword2@";
    private static final String EMAIL_3 = "jane_doe@mail.com";
    private static final String EMAIL_4 = "ricky_roe@mail.com";
    private static final String USERNAME_5 = "TheBestJohnDoe";
    private static final String EMAIL_5 = "bestjohndoe@mail.com";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final long ULTRA_SHORT_EXPIRATION = 1L;
    private static final long ACTION_EXPIRATION = 60000L;
    private static final long ACCESS_EXPIRATION = 900000L;
    private static final long REFRESH_EXPIRATION = 604800000L;
    private static final String SECRET_KEY =
            "eZTQb1Um2KE0dukTWfyHZSq2R3R1SFyqfRFfiReAPn1NHMKUBiTDKc5tajfn";
    private static final long FIRST_USER_ID = 1L;
    private UserRepository userRepository;
    private UserMapper userMapper;
    private AuthenticationManager authenticationManager;
    private PasswordEncoder passwordEncoder;
    private JwtStrategy jwtStrategy;
    private ParamFromHttpRequestUtil paramFromHttpRequestUtil;
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userMapper = mock(UserMapper.class);
        authenticationManager = mock(AuthenticationManager.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtStrategy = mock(JwtStrategy.class);
        paramFromHttpRequestUtil = mock(ParamFromHttpRequestUtil.class);

        PasswordEmailService passwordEmailService = mock(PasswordEmailService.class);
        RegisterConfirmEmailService registerConfirmEmailService =
                mock(RegisterConfirmEmailService.class);
        RoleRepository roleRepository = mock(RoleRepository.class);

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
                ACCESS_EXPIRATION,
                REFRESH_EXPIRATION);
    }

    @Nested
    class AuthenticateUser {
        @Test
        void givenValidLoginRequestDto_whenAuthenticateUserByEmail_thenSuccessfullyLogin()
                throws LoginException {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();
            User user = User.builder()
                    .id(FIRST_USER_ID)
                    .username(USERNAME_1)
                    .password(PASSWORD_1_DB)
                    .email(EMAIL_1)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();
            List<GrantedAuthority> grantedAuthorities = List.of(
                    new SimpleGrantedAuthority(role.getName().name()));

            //when
            when(userRepository.findByEmail(EMAIL_1)).thenReturn(Optional.of(user));
            when(authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            USERNAME_1, PASSWORD_1)))
                    .thenReturn(new UsernamePasswordAuthenticationToken(
                            USERNAME_1, PASSWORD_1_DB, grantedAuthorities));

            JwtAccessUtil jwtAccessUtil = mock(JwtAccessUtil.class);
            when(jwtStrategy.getStrategy(JwtType.ACCESS))
                    .thenReturn(jwtAccessUtil);

            JwtRefreshUtil jwtRefreshUtil = mock(JwtRefreshUtil.class);
            when(jwtStrategy.getStrategy(JwtType.REFRESHMENT))
                    .thenReturn(jwtRefreshUtil);

            //then
            LoginRequest validLoginRequest =
                    new LoginRequest(EMAIL_1, PASSWORD_1);
            HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
            LoginResponse loginResponseResult = authenticationService.authenticateUser(
                    validLoginRequest, httpServletResponse);
            assertEquals(new LoginResponse(LOGIN_SUCCESS), loginResponseResult);

            //verify
            verify(userRepository, times(1)).findByEmail(EMAIL_1);
            verify(authenticationManager, times(1)).authenticate(
                    new UsernamePasswordAuthenticationToken(
                            USERNAME_1, PASSWORD_1));
            verify(jwtStrategy, times(1)).getStrategy(JwtType.ACCESS);
            verify(jwtStrategy, times(1)).getStrategy(JwtType.REFRESHMENT);
        }

        @Test
        void givenNotExistingEmail_whenAuthenticateUserByEmail_thenThrowEntityNotFound() {
            //when
            when(userRepository.findByEmail(EMAIL_5))
                    .thenThrow(new EntityNotFoundException(
                            "No user with email " + EMAIL_5 + " found"));

            //then
            LoginRequest invalidLoginRequest =
                    new LoginRequest(EMAIL_5, PASSWORD_1);
            HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
            EntityNotFoundException userNotFoundException =
                    assertThrows(EntityNotFoundException.class, () ->
                            authenticationService
                                    .authenticateUser(invalidLoginRequest, httpServletResponse));
            assertEquals("No user with email "
                    + EMAIL_5 + " found", userNotFoundException.getMessage());

            //verify
            verify(userRepository, times(1)).findByEmail(EMAIL_5);
        }

        @Test
        void givenNotExistingUsername_whenAuthenticateUserByUsername_thenThrowEntityNotFound() {
            //when
            when(userRepository.findByUsername(USERNAME_5)).thenThrow(
                    new EntityNotFoundException("No user with username "
                    + USERNAME_5 + " found"));

            //then
            LoginRequest invalidLoginRequest =
                    new LoginRequest(USERNAME_5, PASSWORD_1);
            HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
            EntityNotFoundException userNotFoundException =
                    assertThrows(EntityNotFoundException.class, () ->
                            authenticationService
                                    .authenticateUser(invalidLoginRequest, httpServletResponse));
            assertEquals("No user with username "
                    + USERNAME_5 + " found", userNotFoundException.getMessage());

            //verify
            verify(userRepository, times(1)).findByUsername(USERNAME_5);
        }

        @Test
        void givenValidLoginRequestDto_whenAuthenticateUserByUsername_thenSuccessfullyLogin()
                throws LoginException {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();
            User user = User.builder()
                    .id(FIRST_USER_ID)
                    .username(USERNAME_1)
                    .password(PASSWORD_1_DB)
                    .email(EMAIL_1)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();
            List<GrantedAuthority> grantedAuthorities = List.of(
                    new SimpleGrantedAuthority(role.getName().name()));

            //when
            when(userRepository.findByUsername(USERNAME_1)).thenReturn(Optional.of(user));
            when(authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            USERNAME_1, PASSWORD_1)))
                    .thenReturn(new UsernamePasswordAuthenticationToken(
                            USERNAME_1, PASSWORD_1_DB, grantedAuthorities));

            JwtAccessUtil jwtAccessUtil = mock(JwtAccessUtil.class);
            when(jwtStrategy.getStrategy(JwtType.ACCESS))
                    .thenReturn(jwtAccessUtil);

            JwtRefreshUtil jwtRefreshUtil = mock(JwtRefreshUtil.class);
            when(jwtStrategy.getStrategy(JwtType.REFRESHMENT))
                    .thenReturn(jwtRefreshUtil);

            //then
            LoginRequest validLoginRequest =
                    new LoginRequest(USERNAME_1, PASSWORD_1);
            HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
            LoginResponse loginResponseResult = authenticationService.authenticateUser(
                    validLoginRequest, httpServletResponse);
            assertEquals(new LoginResponse(LOGIN_SUCCESS), loginResponseResult);

            //verify
            verify(userRepository, times(1)).findByUsername(USERNAME_1);
            verify(authenticationManager, times(1)).authenticate(
                    new UsernamePasswordAuthenticationToken(
                            USERNAME_1, PASSWORD_1));
            verify(jwtStrategy, times(1)).getStrategy(JwtType.ACCESS);
            verify(jwtStrategy, times(1)).getStrategy(JwtType.REFRESHMENT);
        }

        @Test
        void givenLockedUser_whenAuthenticateUserByEmail_thenThrowLoginException() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();
            User lockedUser = User.builder()
                    .id(FIRST_USER_ID)
                    .username(USERNAME_1)
                    .password(PASSWORD_1_DB)
                    .email(EMAIL_1)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(false)
                    .isAccountNonLocked(false)
                    .build();

            //when
            when(userRepository.findByEmail(EMAIL_3))
                    .thenReturn(Optional.of(lockedUser));

            //then
            LoginRequest lockedUserLoginRequest =
                    new LoginRequest(EMAIL_3, PASSWORD_1);
            HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
            LoginException loginException = assertThrows(LoginException.class,
                    () -> authenticationService.authenticateUser(
                    lockedUserLoginRequest, httpServletResponse));

            assertEquals(ACCOUNT_IS_LOCKED, loginException.getMessage());

            //verify
            verify(userRepository, times(1)).findByEmail(EMAIL_3);
        }

        @Test
        void givenNonActivatedUser_whenAuthenticateUserByEmail_thenThrowLoginException() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();
            User disabledUser = User.builder()
                    .id(FIRST_USER_ID)
                    .username(USERNAME_1)
                    .password(PASSWORD_1_DB)
                    .email(EMAIL_1)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(false)
                    .isAccountNonLocked(true)
                    .build();

            //when
            when(userRepository.findByEmail(EMAIL_4))
                    .thenReturn(Optional.of(disabledUser));

            //then
            LoginRequest disabledUserRequest =
                    new LoginRequest(EMAIL_4, PASSWORD_1);
            HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
            LoginException loginException = assertThrows(LoginException.class,
                    () -> authenticationService.authenticateUser(
                            disabledUserRequest, httpServletResponse));

            assertEquals(REGISTERED_BUT_NOT_ACTIVATED, loginException.getMessage());

            //verify
            verify(userRepository, times(1)).findByEmail(EMAIL_4);
        }

        @Test
        void givenInvalidPassword_whenAuthenticateUserByEmail_thenThrowLoginException() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();
            User user = User.builder()
                    .id(FIRST_USER_ID)
                    .username(USERNAME_1)
                    .password(PASSWORD_1_DB)
                    .email(EMAIL_1)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();

            //when
            when(userRepository.findByEmail(EMAIL_1)).thenReturn(Optional.of(user));
            when(authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            USERNAME_1, PASSWORD_2)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            //then
            LoginRequest invalidLoginRequest =
                    new LoginRequest(EMAIL_1, PASSWORD_2);
            HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);

            assertThrows(LoginException.class, () -> authenticationService.authenticateUser(
                    invalidLoginRequest, httpServletResponse));

            //verify
            verify(userRepository, times(1)).findByEmail(EMAIL_1);
            verify(authenticationManager, times(1)).authenticate(
                    new UsernamePasswordAuthenticationToken(
                            USERNAME_1, PASSWORD_2));
        }

        @Test
        void givenInvalidPassword_whenAuthenticateUserByUsername_thenThrowLoginException() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();
            User user = User.builder()
                    .id(FIRST_USER_ID)
                    .username(USERNAME_1)
                    .password(PASSWORD_1_DB)
                    .email(EMAIL_1)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();

            //when
            when(userRepository.findByUsername(USERNAME_1)).thenReturn(Optional.of(user));
            when(authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            USERNAME_1, PASSWORD_2)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            //then
            LoginRequest invalidLoginRequest =
                    new LoginRequest(USERNAME_1, PASSWORD_2);
            HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);

            assertThrows(LoginException.class, () -> authenticationService.authenticateUser(
                    invalidLoginRequest, httpServletResponse));

            //verify
            verify(userRepository, times(1)).findByUsername(USERNAME_1);
            verify(authenticationManager, times(1)).authenticate(
                    new UsernamePasswordAuthenticationToken(
                            USERNAME_1, PASSWORD_2));
        }
    }

    @Nested
    class SendPasswordResetLink {
        @Test
        void givenAnEmailOfEnabledUser_whenSendPasswordResetLink_thenSuccessfullySendLink()
                throws LoginException {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();
            User user = User.builder()
                    .id(FIRST_USER_ID)
                    .username(USERNAME_1)
                    .password(PASSWORD_1_DB)
                    .email(EMAIL_1)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();

            //when
            when(userRepository.findByEmail(EMAIL_1)).thenReturn(Optional.of(user));

            //then
            assertEquals(new PasswordResetLinkResponse(SEND_LINK_TO_RESET_PASSWORD),
                    authenticationService.sendPasswordResetLink(EMAIL_1));

            //verify
            verify(userRepository, times(1)).findByEmail(EMAIL_1);
        }

        @Test
        void givenAUsernameOfEnabledUser_whenSendPasswordResetLink_thenSuccessfullySendLink()
                throws LoginException {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();
            User user = User.builder()
                    .id(FIRST_USER_ID)
                    .username(USERNAME_1)
                    .password(PASSWORD_1_DB)
                    .email(EMAIL_1)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();

            //when
            when(userRepository.findByUsername(USERNAME_1)).thenReturn(Optional.of(user));

            //then
            assertEquals(new PasswordResetLinkResponse(SEND_LINK_TO_RESET_PASSWORD),
                    authenticationService.sendPasswordResetLink(USERNAME_1));

            //verify
            verify(userRepository, times(1)).findByUsername(USERNAME_1);
        }

        @Test
        void givenAnEmailOfLockedUser_whenSendPasswordResetLink_thenThrowLoginException() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();
            User lockedUser = User.builder()
                    .id(FIRST_USER_ID)
                    .username(USERNAME_1)
                    .password(PASSWORD_1_DB)
                    .email(EMAIL_1)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(false)
                    .isAccountNonLocked(false)
                    .build();

            //when
            when(userRepository.findByEmail(EMAIL_3))
                    .thenReturn(Optional.of(lockedUser));

            //then
            LoginException loginException = assertThrows(LoginException.class,
                    () -> authenticationService.sendPasswordResetLink(EMAIL_3));
            assertEquals(ACCOUNT_IS_LOCKED, loginException.getMessage());

            //verify
            verify(userRepository, times(1)).findByEmail(EMAIL_3);
        }

        @Test
        void givenAnEmailOfNonActiveUser_whenSendPasswordResetLink_thenThrowLoginException() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();
            User disabledUser = User.builder()
                    .id(FIRST_USER_ID)
                    .username(USERNAME_1)
                    .password(PASSWORD_1_DB)
                    .email(EMAIL_1)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(false)
                    .isAccountNonLocked(true)
                    .build();

            //when
            when(userRepository.findByEmail(EMAIL_4))
                    .thenReturn(Optional.of(disabledUser));

            //then
            LoginException loginException = assertThrows(LoginException.class,
                    () -> authenticationService.sendPasswordResetLink(EMAIL_4));
            assertEquals(REGISTERED_BUT_NOT_ACTIVATED, loginException.getMessage());

            //verify
            verify(userRepository, times(1)).findByEmail(EMAIL_4);
        }

        @Test
        void givenAnEmailOfNonExistingUser_whenSendPasswordResetLink_thenThrowEntityNotFound() {
            //when
            when(userRepository.findByEmail(EMAIL_5))
                    .thenThrow(new EntityNotFoundException(
                            "No user with email " + EMAIL_5 + " found"));

            //then
            EntityNotFoundException userNotFoundException =
                    assertThrows(EntityNotFoundException.class, () ->
                            authenticationService
                                    .sendPasswordResetLink(EMAIL_5));
            assertEquals("No user with email "
                    + EMAIL_5 + " found", userNotFoundException.getMessage());

            //verify
            verify(userRepository, times(1)).findByEmail(EMAIL_5);
        }

        @Test
        void givenAUsernameOfNonExistingUser_whenSendPasswordResetLink_thenThrowEntityNotFound() {
            //when
            when(userRepository.findByUsername(USERNAME_5))
                    .thenThrow(new EntityNotFoundException(
                            "No user with username " + USERNAME_5 + " found"));

            //then
            EntityNotFoundException userNotFoundException =
                    assertThrows(EntityNotFoundException.class, () ->
                            authenticationService
                                    .sendPasswordResetLink(USERNAME_5));
            assertEquals("No user with username "
                    + USERNAME_5 + " found", userNotFoundException.getMessage());

            //verify
            verify(userRepository, times(1)).findByUsername(USERNAME_5);
        }
    }

    @Nested
    class ConfirmResetPassword {
        @Test
        void givenGoodToken_whenConfirmResetPassword_thenSuccessfullyNewPassword() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();
            User user = User.builder()
                    .id(FIRST_USER_ID)
                    .username(USERNAME_1)
                    .password(PASSWORD_1_DB)
                    .email(EMAIL_1)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();
            JwtAbstractUtil jwtActionUtil =
                    new JwtActionUtil(SECRET_KEY, ACTION_EXPIRATION);
            String goodToken = jwtActionUtil.generateToken(EMAIL_1);
            HttpServletRequest request = mock(HttpServletRequest.class);

            //when
            when(paramFromHttpRequestUtil
                    .parseRandomParameterAndToken(request))
                        .thenReturn(goodToken);
            when(jwtStrategy.getStrategy(JwtType.ACTION)).thenReturn(jwtActionUtil);
            when(userRepository.findByEmail(EMAIL_1)).thenReturn(Optional.of(user));

            //then
            assertEquals(new ResetLinkSentResponse(CHECK_YOUR_EMAIL),
                    authenticationService.confirmResetPassword(request));

            //verify
            verify(paramFromHttpRequestUtil, times(1)).parseRandomParameterAndToken(request);
            verify(jwtStrategy, times(1)).getStrategy(JwtType.ACTION);
            verify(userRepository, times(1)).findByEmail(EMAIL_1);
        }

        @Test
        void givenBadToken_whenConfirmResetPassword_thenThrowLinkExpiredException() {
            //given
            JwtAbstractUtil jwtBadActionUtil =
                    new JwtActionUtil(SECRET_KEY, ULTRA_SHORT_EXPIRATION);
            String badToken = jwtBadActionUtil.generateToken(EMAIL_1);
            HttpServletRequest request = mock(HttpServletRequest.class);

            //when
            when(paramFromHttpRequestUtil
                    .parseRandomParameterAndToken(request))
                    .thenReturn(badToken);
            when(jwtStrategy.getStrategy(JwtType.ACTION)).thenReturn(jwtBadActionUtil);

            //then
            LinkExpiredException linkExpiredException = assertThrows(LinkExpiredException.class,
                    () -> authenticationService.confirmResetPassword(request));
            assertEquals(LINK_EXPIRED, linkExpiredException.getMessage());

            //verify
            verify(paramFromHttpRequestUtil, times(1)).parseRandomParameterAndToken(request);
            verify(jwtStrategy, times(1)).getStrategy(JwtType.ACTION);
        }
    }

    @Nested
    class ChangePassword {
        @Test
        void givenCorrectCurrentAndTwoSameNewPasswords_whenChangePassword_thenSuccess()
                throws PasswordMismatchException {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();
            User user = User.builder()
                    .id(FIRST_USER_ID)
                    .username(USERNAME_1)
                    .password(PASSWORD_1_DB)
                    .email(EMAIL_1)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();
            PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest(
                    PASSWORD_1,
                    PASSWORD_2,
                    PASSWORD_2);

            //when
            when(passwordEncoder.matches(
                    PASSWORD_1, PASSWORD_1_DB)).thenReturn(true);

            //then
            assertEquals(new PasswordChangeResponse(PASSWORD_SET_SUCCESSFULLY),
                    authenticationService.changePassword(user, passwordChangeRequest));

            //verify
            verify(passwordEncoder, times(1))
                    .matches(PASSWORD_1, PASSWORD_1_DB);
        }

        @Test
        void givenIncorrectCurrentAndTwoSameNewPasswords_whenChangePassword_thenException() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();
            User user = User.builder()
                    .id(FIRST_USER_ID)
                    .username(USERNAME_1)
                    .password(PASSWORD_1_DB)
                    .email(EMAIL_1)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();
            PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest(
                    PASSWORD_3,
                    PASSWORD_2,
                    PASSWORD_2);

            //when
            when(passwordEncoder.matches(
                    PASSWORD_3, PASSWORD_1_DB)).thenReturn(false);

            //then
            PasswordMismatchException passwordMismatchException =
                    assertThrows(PasswordMismatchException.class,
                            () -> authenticationService
                                    .changePassword(user, passwordChangeRequest));

            assertEquals(PASSWORD_MISMATCH, passwordMismatchException.getMessage());

            //verify
            verify(passwordEncoder, times(1))
                    .matches(PASSWORD_3, PASSWORD_1_DB);
        }
    }

    @Nested
    class Register {
        @Test
        void givenValidRegistrationDto_whenRegister_thenSuccess() throws RegistrationException {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();
            User user = User.builder()
                    .id(FIRST_USER_ID)
                    .username(USERNAME_1)
                    .password(PASSWORD_1_DB)
                    .email(EMAIL_1)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();
            RegistrationRequest registrationRequest = new RegistrationRequest(
                    USERNAME_1,
                    PASSWORD_1,
                    PASSWORD_1,
                    EMAIL_1,
                    FIRST_NAME,
                    LAST_NAME);

            //when
            when(userMapper.toUser(registrationRequest)).thenReturn(user);

            //then
            assertEquals(new RegistrationResponse(REGISTERED),
                    authenticationService.register(registrationRequest));

            //verify
            verify(userMapper, times(1)).toUser(registrationRequest);
        }

        @Test
        void givenRegDtoWithExistingEmail_whenRegister_thenThrowsRegistrationException() {
            //given
            RegistrationRequest registrationRequest = new RegistrationRequest(
                    USERNAME_1,
                    PASSWORD_1,
                    PASSWORD_1,
                    EMAIL_1,
                    FIRST_NAME,
                    LAST_NAME);

            //when
            when(userRepository.existsByEmail(EMAIL_1)).thenReturn(true);

            //then
            RegistrationException registrationException = assertThrows(RegistrationException.class,
                    () -> authenticationService.register(registrationRequest));
            assertEquals("User with email "
                    + EMAIL_1 + " already exists", registrationException.getMessage());

            //verify
            verify(userRepository, times(1)).existsByEmail(EMAIL_1);
        }

        @Test
        void givenRegDtoWithExistingUsername_whenRegister_thenThrowRegistrationException() {
            //given
            RegistrationRequest registrationRequest = new RegistrationRequest(
                    USERNAME_1,
                    PASSWORD_1,
                    PASSWORD_1,
                    EMAIL_1,
                    FIRST_NAME,
                    LAST_NAME);

            //when
            when(userRepository.existsByUsername(USERNAME_1)).thenReturn(true);

            //then
            RegistrationException registrationException = assertThrows(RegistrationException.class,
                    () -> authenticationService.register(registrationRequest));
            assertEquals("User with username "
                    + USERNAME_1 + " already exists", registrationException.getMessage());

            //verify
            verify(userRepository, times(1)).existsByUsername(USERNAME_1);
        }
    }

    @Nested
    class ConfirmRegistration {
        @Test
        void givenToken_whenConfirmRegistration_thenSuccess() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();
            User user = User.builder()
                    .id(FIRST_USER_ID)
                    .username(USERNAME_1)
                    .password(PASSWORD_1_DB)
                    .email(EMAIL_1)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();
            JwtAbstractUtil jwtActionUtil =
                    new JwtActionUtil(SECRET_KEY, ACTION_EXPIRATION);
            HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
            String token = jwtActionUtil.generateToken(EMAIL_1);

            //when
            when(paramFromHttpRequestUtil
                    .parseRandomParameterAndToken(request)).thenReturn(token);
            when(jwtStrategy.getStrategy(JwtType.ACTION)).thenReturn(jwtActionUtil);
            when(userRepository.findByEmail(EMAIL_1)).thenReturn(Optional.of(user));

            //then
            assertEquals(new RegistrationConfirmationResponse(REGISTRATION_CONFIRMED),
                    authenticationService.confirmRegistration(request));

            //verify
            verify(paramFromHttpRequestUtil, times(1)).parseRandomParameterAndToken(request);
            verify(jwtStrategy, times(1)).getStrategy(JwtType.ACTION);
            verify(userRepository, times(1)).findByEmail(EMAIL_1);
        }
    }
}
