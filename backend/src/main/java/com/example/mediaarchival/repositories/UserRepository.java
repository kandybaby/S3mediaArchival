package com.example.mediaarchival.repositories;

import com.example.mediaarchival.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for {@link UserModel} that extends Spring Data JPA's {@link JpaRepository}.
 * It provides CRUD operations and custom query methods for the UserModel.
 */
@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {

  /**
   * Retrieves a user by their username.
   *
   * @param username the username to search for
   * @return the user with the specified username, or null if no such user exists
   */
  UserModel findByUsername(String username);

  /**
   * Retrieves a user by their refresh token.
   *
   * @param refreshToken the refresh token to search for
   * @return the user with the specified refresh token, or null if no such user exists
   */
  UserModel findByRefreshToken(String refreshToken);
}
