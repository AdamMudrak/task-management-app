package com.example.taskmanagementapp.controllers;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.CHECK_YOUR_EMAIL;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.RANDOM_LINK_STRENGTH;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.REGISTERED;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.REGISTERED_BUT_NOT_ACTIVATED;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.REGISTRATION_CONFIRMED;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.SEND_LINK_TO_RESET_PASSWORD;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.STRENGTH;
import static com.example.taskmanagementapp.constants.validation.ValidationConstants.NEW_PASSWORD_MISMATCH;
import static com.example.taskmanagementapp.constants.validation.ValidationConstants.PASSWORD_COLLISION;

import com.example.taskmanagementapp.dtos.authentication.request.LoginRequest;
import com.example.taskmanagementapp.dtos.authentication.request.PasswordChangeRequest;
import com.example.taskmanagementapp.dtos.authentication.request.PasswordResetLinkRequest;
import com.example.taskmanagementapp.dtos.authentication.request.RegistrationRequest;
import com.example.taskmanagementapp.entities.ParamToken;
import com.example.taskmanagementapp.entities.Role;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.EntityNotFoundException;
import com.example.taskmanagementapp.repositories.ParamTokenRepository;
import com.example.taskmanagementapp.repositories.RoleRepository;
import com.example.taskmanagementapp.repositories.UserRepository;
import com.example.taskmanagementapp.security.jwtutils.strategy.JwtStrategy;
import com.example.taskmanagementapp.security.jwtutils.strategy.JwtType;
import com.example.taskmanagementapp.services.utils.RandomStringUtil;
import com.example.taskmanagementapp.services.utils.TestCaptureService;
import com.example.taskmanagementapp.testutils.Constants;
import com.example.taskmanagementapp.testutils.ObjectFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class AuthControllerTest {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(STRENGTH);
    private static final String MOCK_EMAIL = "mock@mail.com";
    private static final String MOCK_USERNAME = "mockUsername";
    private static final String MOCK_PASSWORD = "Mock_Password1";
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ParamTokenRepository paramTokenRepository;
    @Autowired
    private JwtStrategy jwtStrategy;
    private User user;
    private User disabledUser;
    private User notActivatedUser;

    @BeforeAll
    void setUpBeforeAll(@Autowired WebApplicationContext webApplicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        Role role = roleRepository.save(ObjectFactory.getUserRole());
        user = userRepository.save(ObjectFactory.getUser1(role));
        disabledUser = userRepository.save(ObjectFactory.getDisabledUser(role));
        notActivatedUser = userRepository.save(ObjectFactory.getNotActiveUser(role));
    }

    @AfterAll
    void tearDownAfterAll() {
        paramTokenRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Nested
    class RegisterUser {
        @Test
        void givenInvalidRegistrationRequest_whenRegister_ThenThrowGroupOfExceptions()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    ObjectFactory.getRegistrationRequestWithAllFieldsWrong());

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/register")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andReturn();
            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            Assertions.assertEquals(Constants.EXPECTED_ERRORS_ON_REGISTER.size(),
                    jsonNode.get("errors").size());
            for (JsonNode node : jsonNode.get("errors")) {
                Assertions.assertTrue(Constants.EXPECTED_ERRORS_ON_REGISTER
                        .contains(node.asText()));
            }
        }

        @Test
        void givenNewRegistrationRequest_whenRegister_thenReturnSuccess() throws Exception {
            successfulRegistration(ObjectFactory.getRegistrationRequest1());
        }

        @Test
        void givenRegistrationRequestWithExistingUsername_whenRegister_thenConflict()
                throws Exception {
            RegistrationRequest registrationRequest =
                    ObjectFactory.getRegistrationRequestWithExistingUsername();
            String jsonRequest = objectMapper.writeValueAsString(registrationRequest);

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/register")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isConflict())
                    .andReturn();
            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            Assertions.assertEquals("CONFLICT", jsonNode.get("status").asText());
            Assertions.assertEquals("User with username "
                            + registrationRequest.username() + " already exists.",
                    jsonNode.get("errors").asText());
        }

        @Test
        void givenRegistrationRequestWithExistingEmail_whenRegister_thenConflict()
                throws Exception {
            RegistrationRequest registrationRequest =
                    ObjectFactory.getRegistrationRequestWithExistingEmail();
            String jsonRequest = objectMapper.writeValueAsString(registrationRequest);

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/register")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isConflict())
                    .andReturn();
            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            Assertions.assertEquals("User with email "
                            + registrationRequest.email() + " already exists.",
                    jsonNode.get("errors").asText());
        }
    }

    @Nested
    class ConfirmRegistration {
        @Test
        void givenSuccessfulRegistrationRequest_whenConfirmRegistration_thenSuccess()
                throws Exception {
            RegistrationRequest registrationRequest = ObjectFactory.getRegistrationRequest2();
            successfulRegistration(registrationRequest);
            String[] paramTokenPair = TestCaptureService.getLastValue();
            if (paramTokenPair == null) {
                throw new RuntimeException("Registration process proceeded with failures");
            }
            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/auth/register-success")
                            .param(paramTokenPair[0], paramTokenPair[1]))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn();
            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            TestCaptureService.clear();
            Assertions.assertEquals(REGISTRATION_CONFIRMED, jsonNode.get("response").asText());
        }

        @Test
        void givenRegistrationRequestForNotExistingUser_whenConfirmRegistration_thenNotFound()
                throws Exception {
            String randomParam = RandomStringUtil.generateRandomString(RANDOM_LINK_STRENGTH);
            String token = jwtStrategy.getStrategy(JwtType.ACTION).generateToken(MOCK_EMAIL);
            ParamToken paramToken = new ParamToken();
            paramToken.setParameter(randomParam);
            paramToken.setActionToken(token);
            paramTokenRepository.save(paramToken);

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/auth/register-success")
                            .param(randomParam, token))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andReturn();
            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            Assertions.assertEquals("User with email " + MOCK_EMAIL + " was not found.",
                    jsonNode.get("errors").asText());
        }

        @Test
        void givenRegistrationRequestWithNotSavedParam_whenConfirmRegistration_thenNotFound()
                throws Exception {
            String randomParam = RandomStringUtil.generateRandomString(RANDOM_LINK_STRENGTH);
            String token = jwtStrategy.getStrategy(JwtType.ACTION).generateToken(MOCK_EMAIL);

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/auth/register-success")
                            .param(randomParam, token))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andReturn();
            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            Assertions.assertEquals("No such request was found... "
                            + "The link might be expired or forged.",
                    jsonNode.get("errors").asText());
        }

        @Test
        void givenRegistrationRequestWithNoParamAtAll_whenConfirmRegistration_thenNotFound()
                throws Exception {
            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/auth/register-success"))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andReturn();
            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            Assertions.assertEquals("Wasn't able to parse link...Might be expired or forged.",
                    jsonNode.get("errors").asText());
        }
    }

    @Nested
    class Login {
        @Test
        void givenExistingUser_whenLoginWithEmail_thenGetResponseWithTokensInCookies()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    ObjectFactory.getExistingLoginByEmailRequest());
            List<String> expectedCookies = new ArrayList<>();
            expectedCookies.add("refreshToken");
            expectedCookies.add("accessToken");

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/login")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn();

            Cookie[] cookies = result.getResponse().getCookies();
            Assertions.assertEquals(expectedCookies.size(), cookies.length);
            for (Cookie cookie : cookies) {
                Assertions.assertTrue(expectedCookies.contains(cookie.getName()));
            }
        }

        @Test
        void givenExistingUser_whenLoginWithUsername_thenGetResponseWithTokensInCookies()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    ObjectFactory.getExistingLoginByUsernameRequest());
            List<String> expectedCookies = new ArrayList<>();
            expectedCookies.add("refreshToken");
            expectedCookies.add("accessToken");

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/login")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn();

            Cookie[] cookies = result.getResponse().getCookies();
            Assertions.assertEquals(expectedCookies.size(), cookies.length);
            for (Cookie cookie : cookies) {
                Assertions.assertTrue(expectedCookies.contains(cookie.getName()));
            }
        }

        @Test
        void givenExistingUserWithWrongPassword_whenLoginWithUsername_thenLoginException()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    new LoginRequest(Constants.USERNAME_1, MOCK_PASSWORD));

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/login")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isForbidden())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            Assertions.assertEquals(
                    "Either login or password is invalid.", jsonNode.get("errors").asText());
        }

        @Test
        void givenExistingDisabledUser_whenLoginWithUsername_thenLoginException()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    new LoginRequest(Constants.USERNAME_3, Constants.PASSWORD_1));

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/login")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isForbidden())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            Assertions.assertEquals(
                    "Your account is locked. Consider contacting support team.",
                    jsonNode.get("errors").asText());
        }

        @Test
        void givenExistingUserWithWrongPassword_whenLoginWithEmail_thenLoginException()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    new LoginRequest(Constants.EMAIL_1, MOCK_PASSWORD));

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/login")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isForbidden())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            Assertions.assertEquals(
                    "Either login or password is invalid.", jsonNode.get("errors").asText());
        }

        @Test
        void givenExistingUserWithWrongLogin_whenLoginWithEmail_thenLoginException()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    new LoginRequest(MOCK_EMAIL, Constants.PASSWORD_1));

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/login")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isForbidden())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            Assertions.assertEquals(
                    "Either login or password is invalid.", jsonNode.get("errors").asText());
        }

        @Test
        void givenExistingUserWithWrongLogin_whenLoginWithUsername_thenLoginException()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    new LoginRequest(MOCK_USERNAME, Constants.PASSWORD_1));

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/login")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isForbidden())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            Assertions.assertEquals(
                    "Either login or password is invalid.", jsonNode.get("errors").asText());
        }
    }

    @Nested
    class ChangePassword {
        @WithUserDetails(Constants.USERNAME_1)
        @Test
        void givenLoggedInUser_whenChangePassword_thenSuccess() throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(new PasswordChangeRequest(
                    Constants.PASSWORD_1,
                    Constants.PASSWORD_2,
                    Constants.PASSWORD_2));

            mockMvc.perform(MockMvcRequestBuilders.post("/auth/change-password")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());

            User user = userRepository.findByUsername(Constants.USERNAME_1)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "No user with username " + Constants.USERNAME_1));
            Assertions.assertTrue(encoder.matches(Constants.PASSWORD_2, user.getPassword()));

            //reset this test
            user.setPassword(Constants.PASSWORD_1_DB);
            userRepository.save(user);
        }

        @WithUserDetails(Constants.USERNAME_1)
        @Test
        void givenLoggedInUser_whenChangePassword_repeatPasswordDoesNotMatch_thenFail()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(new PasswordChangeRequest(
                    Constants.PASSWORD_1,
                    Constants.PASSWORD_2,
                    MOCK_PASSWORD));

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/auth/change-password")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andReturn();
            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            Assertions.assertEquals(NEW_PASSWORD_MISMATCH, jsonNode.get("errors").get(0).asText());
        }

        @WithUserDetails(Constants.USERNAME_1)
        @Test
        void givenLoggedInUser_whenChangePassword_currentPasswordDoesNotMatch_thenFail()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(new PasswordChangeRequest(
                    MOCK_PASSWORD,
                    Constants.PASSWORD_2,
                    Constants.PASSWORD_2));

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/auth/change-password")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isConflict())
                    .andReturn();
            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            Assertions.assertEquals("Wrong password. Try resetting "
                    + "password and using a new random password.", jsonNode.get("errors").asText());
        }

        @WithUserDetails(Constants.USERNAME_1)
        @Test
        void givenLoggedInUser_whenChangePassword_currentPasswordCollidesWithNewPassword_thenFail()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(new PasswordChangeRequest(
                    Constants.PASSWORD_1,
                    Constants.PASSWORD_1,
                    Constants.PASSWORD_1));

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/auth/change-password")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andReturn();
            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            Assertions.assertEquals(PASSWORD_COLLISION, jsonNode.get("errors").get(0).asText());
        }
    }

    @Nested
    class InitiatePasswordReset {
        @Test
        void givenNonExistingUsername_whenInitiatePasswordReset_thenFail()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    new PasswordResetLinkRequest(MOCK_USERNAME));

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/forgot-password")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            Assertions.assertEquals("No user with username " + MOCK_USERNAME + " found.",
                    jsonNode.get("errors").asText());
        }

        @Test
        void givenNonExistingEmail_whenInitiatePasswordReset_thenFail()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    new PasswordResetLinkRequest(MOCK_EMAIL));

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/forgot-password")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            Assertions.assertEquals("No user with email " + MOCK_EMAIL + " found.",
                    jsonNode.get("errors").asText());
        }

        @Test
        void givenEmailOfDisabledUser_whenInitiatePasswordReset_thenFail()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    new PasswordResetLinkRequest(disabledUser.getEmail()));

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/forgot-password")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isForbidden())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            Assertions.assertEquals("Your account is locked. Consider contacting support team.",
                    jsonNode.get("errors").asText());
        }

        @Test
        void givenUsernameOfDisabledUser_whenInitiatePasswordReset_thenFail()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    new PasswordResetLinkRequest(disabledUser.getUsername()));

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/forgot-password")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isForbidden())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            Assertions.assertEquals("Your account is locked. Consider contacting support team.",
                    jsonNode.get("errors").asText());
        }

        @Test
        void givenUsernameOfNotActivatedUser_whenInitiatePasswordReset_thenFailButCanActivateAfter()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    new PasswordResetLinkRequest(notActivatedUser.getUsername()));

            //provoke failure using not activated user
            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/forgot-password")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isForbidden())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            Assertions.assertEquals(REGISTERED_BUT_NOT_ACTIVATED,
                    jsonNode.get("errors").asText());

            //Check if you can activate after failure
            String[] paramTokenPair = TestCaptureService.getLastValue();
            if (paramTokenPair == null) {
                throw new RuntimeException("Registration process proceeded with failures");
            }
            result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/auth/register-success")
                            .param(paramTokenPair[0], paramTokenPair[1]))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn();
            jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            TestCaptureService.clear();
            Assertions.assertEquals(REGISTRATION_CONFIRMED, jsonNode.get("response").asText());
        }

        @Test
        void givenEmailOfNotActivatedUser_whenInitiatePasswordReset_thenFailButCanActivateAfter()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    new PasswordResetLinkRequest(notActivatedUser.getEmail()));

            //provoke failure using not activated user
            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/forgot-password")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isForbidden())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            Assertions.assertEquals(REGISTERED_BUT_NOT_ACTIVATED,
                    jsonNode.get("errors").asText());

            //Check if you can activate after failure
            String[] paramTokenPair = TestCaptureService.getLastValue();
            if (paramTokenPair == null) {
                throw new RuntimeException("Registration process proceeded with failures");
            }
            result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/auth/register-success")
                            .param(paramTokenPair[0], paramTokenPair[1]))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn();
            jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            TestCaptureService.clear();
            Assertions.assertEquals(REGISTRATION_CONFIRMED, jsonNode.get("response").asText());
        }
    }

    /**
     * This test class will trigger both - initiatePasswordReset and resetPassword simultaneously
     * as it's the only logical way - these functions should work together exclusively.
     */
    @Nested
    class ResetPasswordFullFlow {

        @Test
        void givenExistingUser_whenInitiatePasswordResetAndResetPassword_thenSuccess()
                throws Exception {
            //initiate password reset
            String jsonRequest = objectMapper.writeValueAsString(
                    new PasswordResetLinkRequest(user.getEmail()));

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/forgot-password")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            Assertions.assertEquals(SEND_LINK_TO_RESET_PASSWORD, jsonNode.get("response").asText());

            //actually reset the password
            if (TestCaptureService.getLastValue() == null) {
                throw new RuntimeException("Initiate reset process proceeded with failures");
            }
            String[] paramTokenPair = TestCaptureService.getLastValue();
            TestCaptureService.clear();
            result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/auth/reset-password")
                            .param(paramTokenPair[0], paramTokenPair[1]))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn();

            jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            Assertions.assertEquals(CHECK_YOUR_EMAIL, jsonNode.get("response").asText());

            //verify that login with a new password works
            jsonRequest = objectMapper.writeValueAsString(new LoginRequest(Constants.EMAIL_1,
                    TestCaptureService.getLastValue()[0]));
            TestCaptureService.clear();
            List<String> expectedCookies = new ArrayList<>();
            expectedCookies.add("refreshToken");
            expectedCookies.add("accessToken");

            result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/login")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn();

            Cookie[] cookies = result.getResponse().getCookies();
            Assertions.assertEquals(expectedCookies.size(), cookies.length);
            for (Cookie cookie : cookies) {
                Assertions.assertTrue(expectedCookies.contains(cookie.getName()));
            }
        }

        @Test
        void givenForgedUri_whenResetPassword_thenThrowException() throws Exception {
            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/auth/reset-password")
                            .param("pseudoParam", "pseudoToken"))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            Assertions.assertEquals("No such request was found... "
                            + "The link might be expired or forged.",
                    jsonNode.get("errors").asText());
        }

        @Test
        void givenEmptyUri_whenResetPassword_thenThrowException() throws Exception {
            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/auth/reset-password"))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            Assertions.assertEquals("Wasn't able to parse link...Might be expired or forged.",
                    jsonNode.get("errors").asText());
        }
    }

    private void successfulRegistration(RegistrationRequest registrationRequest) throws Exception {
        String jsonRequest = objectMapper.writeValueAsString(registrationRequest);

        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.post("/auth/register")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();
        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

        Assertions.assertEquals(REGISTERED, jsonNode.get("response").asText());
    }
}
