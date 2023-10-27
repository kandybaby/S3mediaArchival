package com.example.mediaarchival.repositories;

import com.example.mediaarchival.models.LibraryModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for {@link LibraryModel} that extends Spring Data JPA's {@link JpaRepository}.
 * This interface handles the data access layer for library entities and abstracts the CRUD operations.
 */
@Repository
public interface LibraryRepository extends JpaRepository<LibraryModel, Long> {

  /**
   * Finds a library by its name.
   *
   * @param name the name of the library to search for
   * @return an {@link Optional} describing the found library, or an empty {@code Optional} if no library is found
   */
  Optional<LibraryModel> findByName(String name);

  /**
   * Finds a library by its path.
   *
   * @param path the path of the library to search for
   * @return an {@link Optional} describing the found library, or an empty {@code Optional} if no library is found
   */
  Optional<LibraryModel> findByPath(String path);
}
