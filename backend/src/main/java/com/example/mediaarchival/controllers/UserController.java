package com.example.mediaarchival.controllers;

import com.example.mediaarchival.models.UserModel;
import com.example.mediaarchival.repositories.UserRepository;
import com.example.mediaarchival.responses.LoginResponse;
import com.example.mediaarchival.utils.TokenUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing user-related operations in the application.
 * This includes operations such as updating user details, authenticating users,
 * refreshing JWT tokens, and handling user logout.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  @Autowired
  public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * Updates the user's username or password.
   *
   * @param userRequest The user details to update.
   * @return A response entity indicating the result of the operation.
   */
  @PutMapping("/update")
  public ResponseEntity<String> updateUser(@RequestBody UserModel userRequest) {
    UserModel user = userRepository.findAll().stream().findFirst().orElse(null);

    if (user != null) {
      if (userRequest.getUsername() != null && !userRequest.getUsername().isEmpty()) {
        user.setUsername(userRequest.getUsername());
      }

      if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
        String hashedPassword = passwordEncoder.encode(userRequest.getPassword());
        user.setPassword(hashedPassword);
      }
      userRepository.save(user);
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("User not found.");
    }

    return ResponseEntity.status(HttpStatus.OK).body("User updated successfully.");
  }

  /**
   * Authenticates a user and provides JWT and refresh tokens.
   *
   * @param userRequest The user's login credentials.
   * @param response    The HTTP response object for setting cookies.
   * @return A response entity containing the login response.
   */
  @PostMapping("/authenticate")
  public ResponseEntity<LoginResponse> authenticateUser(
      @RequestBody UserModel userRequest, HttpServletResponse response) {
    // Validate user input (username and password)
    if (userRequest.getUsername() == null
        || userRequest.getUsername().isEmpty()
        || userRequest.getPassword() == null
        || userRequest.getPassword().isEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new LoginResponse());
    }

    // Find the user by username
    UserModel user = userRepository.findByUsername(userRequest.getUsername());

    // Check if the user exists and the provided password matches
    if (user != null && passwordEncoder.matches(userRequest.getPassword(), user.getPassword())) {
      String jwt = TokenUtils.generateJwtToken(user);

      String refresh = TokenUtils.generateRefreshToken();
      Cookie refreshCookie = new Cookie("refreshToken", refresh);
      refreshCookie.setHttpOnly(true);
      refreshCookie.setPath("/");
      refreshCookie.setMaxAge(24 * 60 * 60);
      response.addCookie(refreshCookie);

      Date currentDate = new Date();
      user.setRefreshToken(refresh);
      user.setRefreshExpiry(new Date(currentDate.getTime() + 86400000));
      userRepository.save(user);

      return ResponseEntity.status(HttpStatus.OK).body(new LoginResponse(jwt));
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginResponse());
    }
  }

  /**
   * Refreshes the user's JWT using a refresh token.
   *
   * @param refreshTokenCookie The refresh token cookie.
   * @param response           The HTTP response object for setting new cookies.
   * @return A response entity containing the new login response.
   */
  @PostMapping("/refresh-token")
  public ResponseEntity<LoginResponse> refreshToken(
      @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie,
      HttpServletResponse response) {

    // Validate the refresh token from the cookie
    if (refreshTokenCookie == null || refreshTokenCookie.isEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new LoginResponse());
    }
    Date currentDate = new Date();

    // Find the user by the refresh token
    UserModel user = userRepository.findByRefreshToken(refreshTokenCookie);

    // Check if the user exists and the refresh token matches the current user's refresh token
    if (user != null
        && refreshTokenCookie.equals(user.getRefreshToken())
        && currentDate.before(user.getRefreshExpiry())) {
      String newJwt = TokenUtils.generateJwtToken(user);
      String newRefresh = TokenUtils.generateRefreshToken();

      Cookie refreshCookie = new Cookie("refreshToken", newRefresh);
      refreshCookie.setHttpOnly(true);
      refreshCookie.setPath("/");
      refreshCookie.setMaxAge(24 * 60 * 60);
      response.addCookie(refreshCookie);

      // Update the user's refresh token in the database
      user.setRefreshToken(newRefresh);
      user.setRefreshExpiry(new Date(currentDate.getTime() + 86400000));
      userRepository.save(user);

      // Return the new JWT and refresh token in the response
      return ResponseEntity.status(HttpStatus.OK).body(new LoginResponse(newJwt));
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginResponse());
    }
  }

  /**
   * Logs out the user by revoking the refresh token.
   *
   * @param response The HTTP response object for clearing the refresh token cookie.
   * @return A response entity indicating the result of the logout operation.
   */
  @PostMapping("/logout")
  public ResponseEntity<String> logoutUser(HttpServletResponse response) {
    UserModel user = userRepository.findAll().stream().findFirst().orElse(null);

    if (user != null) {
      user.setRefreshToken(null);
      userRepository.save(user);

      Cookie refreshTokenCookie = new Cookie("refreshToken", null);
      refreshTokenCookie.setMaxAge(0);
      refreshTokenCookie.setPath("/");
      response.addCookie(refreshTokenCookie);

      return ResponseEntity.status(HttpStatus.OK).body("Refresh token revoked successfully.");
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("User not found.");
    }
  }
}
