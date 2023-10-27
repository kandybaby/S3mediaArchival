package com.example.mediaarchival.filters;

import com.example.mediaarchival.enums.ArchivedStatus;
import com.example.mediaarchival.models.LibraryModel;
import com.example.mediaarchival.models.MediaModel;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

/**
 * Class that returns specification objects to be used
 * for filtering results from a Select query to
 * the MediaModel repository
 */
public class MediaSpecifications {

  /**
   * Filters media by their archived status.
   *
   * @param archivedStatus The status to filter by.
   * @return Specification for the given archived status.
   */
  public static Specification<MediaModel> filterByArchivedStatus(ArchivedStatus archivedStatus) {
    return (root, query, criteriaBuilder) -> {
      if (archivedStatus == null) {
        return criteriaBuilder.isTrue(criteriaBuilder.literal(true)); // No filter
      }

      return criteriaBuilder.equal(root.get("archivedStatus"), archivedStatus);
    };
  }

  /**
   * Filters media by the ID of the library they belong to.
   *
   * @param libraryId The library ID to filter by.
   * @return Specification for the given library ID.
   */
  public static Specification<MediaModel> filterByLibraryId(Long libraryId) {
    return (root, query, criteriaBuilder) -> {
      if (libraryId == null) {
        return criteriaBuilder.isTrue(criteriaBuilder.literal(true)); // No filter
      }
      Join<MediaModel, LibraryModel> libraryJoin = root.join("library");
      return criteriaBuilder.equal(libraryJoin.get("id"), libraryId);
    };
  }

  /**
   * Filters media by their recovering status.
   *
   * @param isRecovering The recovering status to filter by.
   * @return Specification for the given recovering status.
   */
  public static Specification<MediaModel> filterByIsRecovering(Boolean isRecovering) {
    return (root, query, criteriaBuilder) -> {
      if (isRecovering == null) {
        return criteriaBuilder.isTrue(criteriaBuilder.literal(true)); // No filter
      }
      return criteriaBuilder.equal(root.get("isRecovering"), isRecovering);
    };
  }

  /**
   * Filters media by their archiving status.
   *
   * @param isArchiving The archiving status to filter by.
   * @return Specification for the given archiving status.
   */
  public static Specification<MediaModel> filterByIsArchiving(Boolean isArchiving) {
    return (root, query, criteriaBuilder) -> {
      if (isArchiving == null) {
        return criteriaBuilder.isTrue(criteriaBuilder.literal(true)); // No filter
      }
      return criteriaBuilder.equal(root.get("isArchiving"), isArchiving);
    };
  }


  /**
   * Searches media items that include the given search term within a subset.
   *
   * @param search The term to search for.
   * @return Specification that matches media items based on the search term.
   */
  public static Specification<MediaModel> searchMediaInSubset(String search) {
    return (root, query, criteriaBuilder) -> {
      if (search == null || search.isEmpty()) {
        return criteriaBuilder.isTrue(criteriaBuilder.literal(true)); // No filter
      }
      String likeSearch = "%" + search.toLowerCase() + "%"; // Convert the search term to lowercase
      return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likeSearch);
    };
  }

  /**
   * Specification for active library jobs given a library ID.
   *
   * @param libraryId The ID of the library to filter jobs by.
   * @return Specification that matches active jobs for the given library ID.
   */
  public static Specification<MediaModel> activeLibraryJobs(Long libraryId) {
    return (root, query, criteriaBuilder) -> {
      if (libraryId == null) {
        return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
      }

      Predicate libraryIdMatch = criteriaBuilder.equal(root.get("library").get("id"), libraryId);
      Predicate isArchiving = criteriaBuilder.isTrue(root.get("isArchiving"));
      Predicate isRecovering = criteriaBuilder.isTrue(root.get("isRecovering"));
      Predicate archivingOrRecovering = criteriaBuilder.or(isArchiving, isRecovering);

      return criteriaBuilder.and(libraryIdMatch, archivingOrRecovering);
    };
  }

  /**
   * Sorts upload jobs based on progress, tarring status, and cancellation status.
   *
   * @return Sort object with orders defined for sorting upload jobs.
   */
  public static Sort getUploadJobsSort() {
    return Sort.by(
        Sort.Order.desc("uploadProgress"),
        Sort.Order.desc("isTarring"),
        Sort.Order.asc("isJobCancelled"));
  }


  /**
   * Sorts download jobs based on progress, restored status, restoring status, and cancellation status.
   *
   * @return Sort object with orders defined for sorting download jobs.
   */
  public static Sort getDownloadJobsSort() {
    return Sort.by(
        Sort.Order.desc("downloadProgress"),
        Sort.Order.asc("isRestored"),
        Sort.Order.asc("isRestoring"),
        Sort.Order.asc("isJobCancelled"));
  }
}
