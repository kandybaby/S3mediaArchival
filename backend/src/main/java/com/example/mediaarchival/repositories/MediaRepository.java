package com.example.mediaarchival.repositories;

import com.example.mediaarchival.enums.ArchivedStatus;
import com.example.mediaarchival.models.MediaModel;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for {@link MediaModel} that extends Spring Data JPA's {@link JpaRepository}
 * and {@link JpaSpecificationExecutor}. This interface facilitates the creation of complex queries
 * and execution of bulk update operations for media entities.
 */
@Repository
public interface MediaRepository
    extends JpaRepository<MediaModel, Long>, JpaSpecificationExecutor<MediaModel> {

  /**
   * Finds all media items associated with a given library ID.
   *
   * @param libraryId the ID of the library
   * @return a list of media items for the specified library
   */
  List<MediaModel> findByLibraryId(Long libraryId);

  /**
   * Finds all media items that match the given specification.
   *
   * @param specification the specification to use for filtering
   * @return a list of media items that match the specification
   */
  List<MediaModel> findAll(Specification<MediaModel> specification);

  /**
   * Retrieves a page of media items that match the given specification along with pagination information.
   *
   * @param specification the criteria used to filter media items
   * @param pageable      the pagination information
   * @return a page of media items that match the criteria
   */
  Page<MediaModel> findAll(Specification<MediaModel> specification, Pageable pageable);

  /**
   * Finds a single media item by its path.
   *
   * @param path the file path of the media item
   * @return the media item with the given path or null if not found
   */
  MediaModel findByPath(String path);

  /**
   * Finds media items by their associated library ID and a list of archived statuses.
   *
   * @param libraryId          the ID of the library
   * @param archivedStatusList the list of archived statuses to filter by
   * @return a list of media items that match the library ID and are within the specified archived statuses
   */
  List<MediaModel> findByLibraryIdAndArchivedStatusIn(Long libraryId, List<ArchivedStatus> archivedStatusList);

  /**
   * Finds media items that are currently being archived.
   *
   * @param isArchiving whether the media item is in the process of archiving
   * @return a list of media items that are being archived
   */
  List<MediaModel> findByIsArchiving(boolean isArchiving);

  /**
   * Finds media items by their download success status.
   *
   * @param downloadSuccess whether the download was successful
   * @return a list of media items with the specified download success status
   */
  List<MediaModel> findByDownloadSuccess(boolean downloadSuccess);

  /**
   * Finds media items that are currently being recovered.
   *
   * @param isRecovering whether the media item is in the process of recovery
   * @return a list of media items that are being recovered
   */
  List<MediaModel> findByIsRecovering(boolean isRecovering);

  /**
   * Finds media items that are currently being restored.
   *
   * @param isRestoring whether the media item is in the process of recovery
   * @return a list of media items that are being recovered
   */
  List<MediaModel> findByIsRestoring(boolean isRestoring);

  /**
   * Updates the upload progress of a media by its ID.
   *
   * @param id             the ID of the media to update
   * @param uploadProgress the new upload progress percentage
   */
  @Modifying
  @Transactional
  @Query("UPDATE MediaModel m SET m.uploadProgress = :uploadProgress WHERE m.id = :id")
  void updateUploadProgressById(Long id, int uploadProgress);

  /**
   * Updates the download progress of a media by its ID.
   *
   * @param id                the ID of the media to update
   * @param downloadProgress  the new download progress percentage
   */
  @Modifying
  @Transactional
  @Query("UPDATE MediaModel m SET m.downloadProgress = :downloadProgress WHERE m.id = :id")
  void updateDownloadProgressById(Long id, int downloadProgress);

  /**
   * Updates the tarring status of a media by its ID.
   *
   * @param id         the ID of the media to update
   * @param isTarring  the new tarring status
   */
  @Modifying
  @Transactional
  @Query("UPDATE MediaModel m SET m.isTarring = :isTarring WHERE m.id = :id")
  void updateIsTarringById(Long id, boolean isTarring);

  /**
   * Updates the restoring status of a media by its ID.
   *
   * @param id            the ID of the media to update
   * @param isRestoring   the new restoring status
   */
  @Modifying
  @Transactional
  @Query("UPDATE MediaModel m SET m.isRestoring = :isRestoring WHERE m.id = :id")
  void updateIsRestoringById(Long id, boolean isRestoring);

  /**
   * Updates the restored status of a media by its ID.
   *
   * @param id           the ID of the media to update
   * @param isRestored   the new restored status
   */
  @Modifying
  @Transactional
  @Query("UPDATE MediaModel m SET m.isRestored = :isRestored WHERE m.id = :id")
  void updateIsRestoredById(Long id, boolean isRestored);

  @Modifying
  @Transactional
  @Query("UPDATE MediaModel m SET m.isRecovering = :isRecovering WHERE m.id = :id")
  void updateIsRecoveringById(Long id, boolean isRecovering);
}