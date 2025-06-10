package com.example.taskmanagementapp.controllers;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.RANDOM_LINK_STRENGTH;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.REGISTERED;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.REGISTRATION_CONFIRMED;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.STRENGTH;
import static com.example.taskmanagementapp.constants.validation.ValidationConstants.NEW_PASSWORD_MISMATCH;

import com.example.taskmanagementapp.dtos.authentication.request.LoginRequest;
import com.example.taskmanagementapp.dtos.authentication.request.PasswordChangeRequest;
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

    @BeforeAll
    void setUpBeforeAll(@Autowired WebApplicationContext webApplicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        Role role = roleRepository.save(ObjectFactory.getUserRole());
        userRepository.save(ObjectFactory.getUser1(role));
        userRepository.save(ObjectFactory.getDisabledUser(role));
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
            String randomParam = RandomStringUtil.generateRandomString(RANDOM_LINK_STRENGTH);
            String token = jwtStrategy.getStrategy(JwtType.ACTION).generateToken(
                    registrationRequest.email());
            saveParamToken(randomParam, token);

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/auth/register-success")
                            .param(randomParam, token))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn();
            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            Assertions.assertEquals(REGISTRATION_CONFIRMED, jsonNode.get("response").asText());
        }

        @Test
        void givenRegistrationRequestForNotExistingUser_whenConfirmRegistration_thenNotFound()
                throws Exception {
            String randomParam = RandomStringUtil.generateRandomString(RANDOM_LINK_STRENGTH);
            String token = jwtStrategy.getStrategy(JwtType.ACTION).generateToken("random@mail.com");
            saveParamToken(randomParam, token);

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/auth/register-success")
                            .param(randomParam, token))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andReturn();
            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            Assertions.assertEquals("User with email random@mail.com was not found.",
                    jsonNode.get("errors").asText());
        }

        @Test
        void givenRegistrationRequestWithNotSavedParam_whenConfirmRegistration_thenNotFound()
                throws Exception {
            String randomParam = RandomStringUtil.generateRandomString(RANDOM_LINK_STRENGTH);
            String token = jwtStrategy.getStrategy(JwtType.ACTION).generateToken("random@mail.com");

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
                    new LoginRequest(Constants.USERNAME, "Wrong_Password1"));

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
                    new LoginRequest(Constants.ANOTHER_USERNAME, Constants.PASSWORD));

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
                    new LoginRequest(Constants.EMAIL, "Wrong_Password1"));

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
                    new LoginRequest("wrong_mail@mail.com", Constants.PASSWORD));

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
                    new LoginRequest("wrong_username", Constants.PASSWORD));

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
        @WithUserDetails(Constants.USERNAME)
        @Test
        void givenLoggedInUser_whenChangePassword_thenSuccess() throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(new PasswordChangeRequest(
                    Constants.PASSWORD,
                    Constants.ANOTHER_PASSWORD,
                    Constants.ANOTHER_PASSWORD));

            mockMvc.perform(MockMvcRequestBuilders.post("/auth/change-password")
                    .content(jsonRequest)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());

            User user = userRepository.findByUsername(Constants.USERNAME)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "No user with username " + Constants.USERNAME));
            Assertions.assertTrue(encoder.matches(Constants.ANOTHER_PASSWORD, user.getPassword()));

            //reset this test
            user.setPassword(Constants.PASSWORD_DB);
            userRepository.save(user);
        }

        @WithUserDetails(Constants.USERNAME)
        @Test
        void givenLoggedInUser_whenChangePassword_repeatPasswordDoesNotMatch_thenFail()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(new PasswordChangeRequest(
                    Constants.PASSWORD,
                    Constants.ANOTHER_PASSWORD,
                    "Wrong_Password1@"));

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/auth/change-password")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andReturn();
            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            Assertions.assertEquals(NEW_PASSWORD_MISMATCH, jsonNode.get("errors").get(0).asText());
        }

        @WithUserDetails(Constants.USERNAME)
        @Test
        void givenLoggedInUser_whenChangePassword_currentPasswordDoesNotMatch_thenFail()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(new PasswordChangeRequest(
                    "Wrong_Password1@",
                    Constants.ANOTHER_PASSWORD,
                    Constants.ANOTHER_PASSWORD));

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/auth/change-password")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isConflict())
                    .andReturn();
            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            Assertions.assertEquals("Wrong password. Try resetting "
                    + "password and using a new random password.", jsonNode.get("errors").asText());
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

    private void saveParamToken(String randomParam, String token) {
        ParamToken paramToken = new ParamToken();
        paramToken.setParameter(randomParam);
        paramToken.setActionToken(token);
        paramTokenRepository.save(paramToken);
    }
}
