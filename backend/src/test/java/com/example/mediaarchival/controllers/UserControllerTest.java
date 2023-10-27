package com.example.mediaarchival.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.example.mediaarchival.models.UserModel;
import com.example.mediaarchival.repositories.UserRepository;
import jakarta.servlet.http.Cookie;

import java.util.Collections;
import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class UserControllerTest {
  private MockMvc mockMvc;

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  private UserController userController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    userController = new UserController(userRepository, passwordEncoder);
    mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
  }

  @Test
  void UserController_testAuthenticateUser_ValidCredentials() throws Exception {
    // Arrange
    UserModel userRequest = new UserModel();
    userRequest.setUsername("TestUser");
    userRequest.setPassword("Password");
    UserModel existingUser = new UserModel();
    existingUser.setUsername("TestUser");
    existingUser.setPassword("HashedPassword"); // Hashed equivalent of "Password"
    when(userRepository.findByUsername(userRequest.getUsername())).thenReturn(existingUser);
    when(passwordEncoder.matches(userRequest.getPassword(), existingUser.getPassword()))
        .thenReturn(true);

    // Act
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.post("/api/users/authenticate")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"TestUser\",\"password\":\"Password\"}");
    MvcResult result = mockMvc.perform(requestBuilder).andReturn();

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void UserController_testAuthenticateUser_InvalidCredentials() throws Exception {
    // Arrange
    UserModel userRequest = new UserModel();
    userRequest.setUsername("TestUser");
    userRequest.setPassword("WrongPassword");
    UserModel existingUser = new UserModel();
    existingUser.setUsername("TestUser");
    existingUser.setPassword("HashedPassword"); // Hashed equivalent of "Password"
    when(userRepository.findByUsername(userRequest.getUsername())).thenReturn(existingUser);
    when(passwordEncoder.matches(userRequest.getPassword(), existingUser.getPassword()))
        .thenReturn(false);

    // Act
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.post("/api/users/authenticate")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"TestUser\",\"password\":\"WrongPassword\"}");
    MvcResult result = mockMvc.perform(requestBuilder).andReturn();

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  void UserController_testAuthenticateUser_MissingCredentials() throws Exception {
    UserModel userRequest = new UserModel();
    // Missing username and password

    RequestBuilder requestBuilder =
            MockMvcRequestBuilders.post("/api/users/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}");  // Empty JSON body
    MvcResult result = mockMvc.perform(requestBuilder).andReturn();

    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  void UserController_testRefreshToken_ValidRefreshToken() throws Exception {
    // Arrange
    String refreshToken = "ValidRefreshToken";
    UserModel userWithRefreshToken = new UserModel();
    userWithRefreshToken.setUsername("TestUser");
    userWithRefreshToken.setRefreshToken(refreshToken);
    Date currentDate = new Date();
    userWithRefreshToken.setRefreshExpiry(new Date(currentDate.getTime() + 86400000));
    when(userRepository.findByRefreshToken(refreshToken)).thenReturn(userWithRefreshToken);

    // Act
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.post("/api/users/refresh-token")
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(new Cookie("refreshToken", refreshToken)); // Set the cookie manually

    MvcResult result = mockMvc.perform(requestBuilder).andReturn();

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    // Add assertions for the response body as needed
  }

  @Test
  void UserController_testRefreshToken_InvalidRefreshToken() throws Exception {
    // Arrange
    String refreshToken = "InvalidRefreshToken";
    when(userRepository.findByRefreshToken(refreshToken)).thenReturn(null);

    // Act
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.post("/api/users/refresh-token")
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(new Cookie("refreshToken", refreshToken)); // Set the cookie manually

    MvcResult result = mockMvc.perform(requestBuilder).andReturn();

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  void UserController_testRefreshToken_ExpiredRefreshToken() throws Exception {
    String expiredRefreshToken = "ExpiredRefreshToken";
    UserModel userWithExpiredToken = new UserModel();
    userWithExpiredToken.setRefreshToken(expiredRefreshToken);
    userWithExpiredToken.setRefreshExpiry(new Date(System.currentTimeMillis() - 86400000)); // Yesterday

    when(userRepository.findByRefreshToken(expiredRefreshToken)).thenReturn(userWithExpiredToken);

    RequestBuilder requestBuilder =
            MockMvcRequestBuilders.post("/api/users/refresh-token")
                    .cookie(new Cookie("refreshToken", expiredRefreshToken));

    MvcResult result = mockMvc.perform(requestBuilder).andReturn();

    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  void UserController_testRefreshToken_MissingRefreshTokenCookie() throws Exception {
    RequestBuilder requestBuilder =
            MockMvcRequestBuilders.post("/api/users/refresh-token");

    MvcResult result = mockMvc.perform(requestBuilder).andReturn();

    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
  }


  @Test
  void UserController_testLogoutUser_UserExists() throws Exception {
    // Arrange
    UserModel existingUser = new UserModel();
    existingUser.setUsername("TestUser");
    existingUser.setRefreshToken("ValidRefreshToken");
    when(userRepository.findAll()).thenReturn(Collections.singletonList(existingUser));

    // Act
    RequestBuilder requestBuilder =
            MockMvcRequestBuilders.post("/api/users/logout")
                    .contentType(MediaType.APPLICATION_JSON);
    MvcResult result = mockMvc.perform(requestBuilder).andReturn();

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    verify(userRepository).save(any(UserModel.class));

    // Verify if refresh token cookie is cleared
    Cookie returnedCookie = result.getResponse().getCookie("refreshToken");
    Assertions.assertNotNull(returnedCookie);
    Assertions.assertEquals(null, returnedCookie.getValue());
    Assertions.assertEquals(0, returnedCookie.getMaxAge());
  }

  @Test
  void UserController_testLogoutUser_UserNotFound() throws Exception {
    when(userRepository.findAll()).thenReturn(Collections.emptyList());

    RequestBuilder requestBuilder =
            MockMvcRequestBuilders.post("/api/users/logout");

    MvcResult result = mockMvc.perform(requestBuilder).andReturn();

    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
  }



}
