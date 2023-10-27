package com.example.mediaarchival.models;

import jakarta.persistence.*;
import java.util.Date;


/**
 * Entity representing a user in the media archival system.
 * This class models the user's attributes and their authentication tokens.
 * As the application is designed for self-hosted scenarios, this model
 * supports a single user instance.
 */
@Entity
@Table(name = "users")
public class UserModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String username;

  @Column(nullable = false)
  private String password;

  @Column(nullable = true)
  private String refreshToken;

  @Column private Date refreshExpiry;

  /**
   * Default constructor for JPA.
   */
  public UserModel() {}

  /**
   * Constructs a new UserModel with the specified username and password.
   *
   * @param username the username for the new user.
   * @param password the password for the new user.
   */
  public UserModel(String username, String password) {
    this.username = username;
    this.password = password;
  }

  /**
   * Gets the unique identifier for the user.
   *
   * @return the user ID.
   */
  public Long getId() {
    return id;
  }

  /**
   * Sets the unique identifier for the user.
   *
   * @param id the ID to set for the user.
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Gets the username for the user.
   *
   * @return the username.
   */
  public String getUsername() {
    return username;
  }

  /**
   * Sets the username for the user. The username is unique and cannot be null.
   *
   * @param username the username to set.
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Gets the password for the user.
   *
   * @return the password.
   */
  public String getPassword() {
    return password;
  }

  /**
   * Sets the password for the user. The password cannot be null.
   *
   * @param password the password to set.
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Gets the refresh token for the user. This token is used to refresh authentication
   * when the current token expires.
   *
   * @return the refresh token, or null if it hasn't been set.
   */
  public String getRefreshToken() {
    return refreshToken;
  }

  /**
   * Sets the refresh token for the user.
   *
   * @param refreshToken the token to set for refreshing authentication.
   */
  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  /**
   * Gets the expiry date for the refresh token.
   *
   * @return the refresh token expiry date, or null if the token hasn't been set or expired.
   */
  public Date getRefreshExpiry() {
    return refreshExpiry;
  }

  /**
   * Sets the expiry date for the refresh token.
   *
   * @param refreshExpiry the expiry date to set for the refresh token.
   */
  public void setRefreshExpiry(Date refreshExpiry) {
    this.refreshExpiry = refreshExpiry;
  }

}
