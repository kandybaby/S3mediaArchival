package com.example.mediaarchival.utils;

import com.example.mediaarchival.models.UserModel;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * A utility class to handle the creation and validation of
 * JSON Web Tokens (JWTs)
 */
public class TokenUtils {
  private static final String SECRET_KEY =
      Base64.getEncoder().encodeToString(new SecureRandom().generateSeed(64));

  private static final long JWT_EXPIRATION_MS = 1800000;

  private static final int TOKEN_LENGTH_BYTES = 64;

  private static final Logger errorLogger = LoggerFactory.getLogger("ERROR_LOGGER");

  /**
   * Generates a JWT token for the given user details.
   *
   * @param userDetails The user details for which the token is to be generated.
   * @return A signed JWT token as a String.
   */
  public static String generateJwtToken(UserModel userDetails) {
    Map<String, Object> claims = new HashMap<>();
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_MS))
        .signWith(SignatureAlgorithm.HS512, getSecretKey())
        .compact();
  }

  /**
   * Generates a secure random refresh token.
   *
   * @return A base64 encoded secure random refresh token.
   */
  public static String generateRefreshToken() {
    SecureRandom secureRandom = new SecureRandom();
    byte[] tokenBytes = new byte[TOKEN_LENGTH_BYTES];
    secureRandom.nextBytes(tokenBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
  }

  /**
   * Validates a JWT token's signature and expiration date.
   *
   * @param token The JWT token to validate.
   * @return true if the token is valid, false otherwise.
   */
  public static boolean validateJwtToken(String token) {
    try {
      Jwts.parser().setSigningKey(getSecretKey()).parseClaimsJws(token);
      return true;
    } catch (Exception e) {
      errorLogger.error(e.getMessage());
      return false;
    }
  }

  /**
   * Extracts authentication information from a JWT token.
   *
   * @param token The JWT token from which to extract authentication details.
   * @return An Authentication object containing the username from the token.
   */
  public static Authentication getAuthenticationFromJwt(String token) {
    try {
      String username =
          Jwts.parser().setSigningKey(getSecretKey()).parseClaimsJws(token).getBody().getSubject();
      return new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
    } catch (Exception e) {
      errorLogger.error(e.getMessage());
      return null;
    }
  }

  /**
   * Retrieves the secret key used for signing JWT tokens.
   *
   * @return The secret key for signing JWT tokens.
   */
  public static Key getSecretKey() {
    byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }


  /**
   * Returns the configured JWT expiration time in milliseconds.
   *
   * @return The JWT expiration time in milliseconds.
   */
  public static long getJwtExpirationMs() {
    return JWT_EXPIRATION_MS;
  }
}
