package com.example.mediaarchival.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a response that is sent back to the client upon a successful login attempt.
 */
public class LoginResponse {
  @JsonProperty("JWT")
  private final String JWT;

  /**
   * Default constructor for creating an empty LoginResponse.
   * This is typically used by frameworks for deserialization purposes.
   */
  public LoginResponse() {
    this.JWT = null;
  }

  /**
   * Constructs a LoginResponse with a specified JWT token.
   *
   * @param JWT The JSON Web Token that represents a successful authentication.
   */
  public LoginResponse(String JWT) {
    this.JWT = JWT;
  }
}
