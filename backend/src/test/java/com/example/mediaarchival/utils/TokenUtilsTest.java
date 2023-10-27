package com.example.mediaarchival.utils;

import static org.junit.jupiter.api.Assertions.*;

import com.example.mediaarchival.models.UserModel;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;

public class TokenUtilsTest {

  @Mock private UserModel mockUserModel;

  @BeforeAll
  static void init() {
    MockitoAnnotations.initMocks(TokenUtilsTest.class);
  }

  @Test
  void testGenerateJwtToken() {
    // Create a UserModel instance
    UserModel userModel = new UserModel();
    userModel.setUsername("testUser");

    // Act
    String token = TokenUtils.generateJwtToken(userModel);

    // Assert
    assertNotNull(token);
    assertTrue(token.length() > 0);

    // Validate the token (this is just a basic validation, you can customize it)
    Claims claims =
        Jwts.parser().setSigningKey(TokenUtils.getSecretKey()).parseClaimsJws(token).getBody();
    assertEquals("testUser", claims.getSubject());
  }

  @Test
  void testGenerateRefreshToken() {
    // Act
    String refreshToken = TokenUtils.generateRefreshToken();

    // Assert
    assertNotNull(refreshToken);
    assertTrue(refreshToken.length() > 0);
  }

  @Test
  void testValidateJwtToken_ValidToken() {
    // Arrange
    String validToken =
        Jwts.builder()
            .setSubject("testUser")
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + TokenUtils.getJwtExpirationMs()))
            .signWith(SignatureAlgorithm.HS512, TokenUtils.getSecretKey())
            .compact();

    // Act
    boolean isValid = TokenUtils.validateJwtToken(validToken);

    // Assert
    assertTrue(isValid);
  }

  @Test
  void testValidateJwtToken_InvalidToken() {
    // Arrange (invalid token)
    String invalidToken = "invalidToken";

    // Act
    boolean isValid = TokenUtils.validateJwtToken(invalidToken);

    // Assert
    assertFalse(isValid);
  }

  @Test
  void testGetAuthenticationFromJwt_ValidToken() {
    // Arrange
    String validToken =
        Jwts.builder()
            .setSubject("testUser")
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + TokenUtils.getJwtExpirationMs()))
            .signWith(SignatureAlgorithm.HS512, TokenUtils.getSecretKey())
            .compact();

    // Act
    Authentication authentication = TokenUtils.getAuthenticationFromJwt(validToken);

    // Assert
    assertNotNull(authentication);
    assertEquals("testUser", authentication.getPrincipal());
    assertNull(authentication.getCredentials());
  }

  @Test
  void testGetAuthenticationFromJwt_InvalidToken() {
    // Arrange (invalid token)
    String invalidToken = "invalidToken";

    // Act
    Authentication authentication = TokenUtils.getAuthenticationFromJwt(invalidToken);

    // Assert
    assertNull(authentication);
  }
}
