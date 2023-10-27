package com.example.mediaarchival.controllers;

import com.example.mediaarchival.enums.ArchivedStatus;
import com.example.mediaarchival.errors.ResourceNotFoundException;
import com.example.mediaarchival.filters.MediaSpecifications;
import com.example.mediaarchival.models.MediaModel;
import com.example.mediaarchival.repositories.MediaRepository;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.model.StorageClass;

/**
 * Controller responsible for managing media objects within the application.
 * Provides endpoints for CRUD operations on media objects, archiving, downloading,
 * and managing the status of media-related jobs.
 */
@RestController
@RequestMapping("/api/media-objects")
public class MediaController {
  private final MediaRepository mediaRepository;

  private final JmsTemplate jmsTemplate;

  private static final Logger errorLogger = LoggerFactory.getLogger("ERROR_LOGGER");

  public MediaController(MediaRepository mediaRepository, JmsTemplate jmsTemplate) {
    this.mediaRepository = mediaRepository;
    this.jmsTemplate = jmsTemplate;
  }

  /**
   * Retrieves a paginated, sorted, and filtered list of media objects based on the provided criteria.
   *
   * @param archivedStatus The status to filter archived media.
   * @param search         The search term for media names.
   * @param libraryId      The ID of the library to filter media.
   * @param isRecovering   Filter for media currently in the process of recovery.
   * @param isArchiving    Filter for media currently in the process of archiving.
   * @param page           The page number for pagination.
   * @param size           The size of each page.
   * @param sortBy         The attribute to sort by.
   * @param sortDirection  The direction of sorting.
   * @return A ResponseEntity containing a page of filtered, sorted media objects.
   */
  @GetMapping
  public ResponseEntity<Page<MediaModel>> getAllMedias(
      @RequestParam(required = false) ArchivedStatus archivedStatus,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) Long libraryId,
      @RequestParam(required = false) Boolean isRecovering,
      @RequestParam(required = false) Boolean isArchiving,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false, defaultValue = "name") String sortBy,
      @RequestParam(required = false, defaultValue = "asc") String sortDirection) {

    Sort sort;
    if ("asc".equalsIgnoreCase(sortDirection)) {
      sort = Sort.by(Sort.Direction.ASC, sortBy);
    } else {
      sort = Sort.by(Sort.Direction.DESC, sortBy);
    }

    if ("uploadJobs".equals(sortBy)) {
      sort = MediaSpecifications.getUploadJobsSort();
    } else if ("downloadJobs".equals(sortBy)) {
      sort = MediaSpecifications.getDownloadJobsSort();
    }

    // Create a Pageable object with sorting
    Pageable pageable = PageRequest.of(page, size, sort);

    // Build the specification based on filters
    Specification<MediaModel> specification =
        MediaSpecifications.filterByArchivedStatus(archivedStatus)
            .and(MediaSpecifications.searchMediaInSubset(search))
            .and(MediaSpecifications.filterByLibraryId(libraryId)) // Apply libraryId filter
            .and(
                MediaSpecifications.filterByIsRecovering(isRecovering)) // Apply isRetrieving filter
            .and(MediaSpecifications.filterByIsArchiving(isArchiving)); // Apply isArchiving filter

    // Fetch paginated, sorted, and filtered media objects
    Page<MediaModel> media = mediaRepository.findAll(specification, pageable);

    return ResponseEntity.ok(media);
  }

  /**
   * Retrieves a specific media object by its ID.
   *
   * @param id The ID of the media object to retrieve.
   * @return A ResponseEntity containing the requested media object.
   * @throws ResourceNotFoundException If no media object is found with the given ID.
   */
  @GetMapping("/{id}")
  public ResponseEntity<MediaModel> getMediaById(@PathVariable Long id) {
    MediaModel media =
        mediaRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + id));
    return ResponseEntity.ok(media);
  }

  /**
   * Deletes a specific media object by its ID.
   *
   * @param id The ID of the media object to delete.
   * @return A ResponseEntity with no content.
   * @throws ResourceNotFoundException If no media object is found with the given ID.
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteMedia(@PathVariable Long id) {
    MediaModel mediaObject =
        mediaRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + id));

    mediaRepository.delete(mediaObject);
    return ResponseEntity.noContent().build();
  }

  /**
   * Bulk delete media objects
   *
   * @param ids The paths of the media objects to delete.
   * @return A ResponseEntity with a confirmation message.
   */
  @PostMapping("/bulk-delete")
  public ResponseEntity<String> bulkDeleteMediaObjects(@RequestBody List<Long> ids) {
    for (long id : ids) {
      try{
        MediaModel media = mediaRepository
            .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + id));
        mediaRepository.delete(media);
      } catch (Exception e){
       errorLogger.error(e.getMessage());
      }
    }

    return ResponseEntity.ok("Deletes successful");
  }

  /**
   * Sends requests to archive a list of media objects identified by their paths.
   *
   * @param paths The paths of the media objects to archive.
   * @return A ResponseEntity with a confirmation message.
   */
  @PostMapping("/archive")
  public ResponseEntity<String> archiveMediaObjects(@RequestBody List<String> paths) {
    for (String path : paths) {
      MediaModel media = mediaRepository.findByPath(path);
      if (!media.isArchiving() && !media.isRecovering()) {
        media.setArchiving(true);
        media.setUploadProgress(-1);
        media.setTarring(false);
        mediaRepository.save(media);
        jmsTemplate.convertAndSend("archivingQueue", media.getPath());
      }
    }

    return ResponseEntity.ok("Archive requests sent successfully");
  }

  /**
   * Prepares a list of media objects for download by sending the appropriate requests.
   *
   * @param paths The paths of the media objects to prepare for download.
   * @return A ResponseEntity with a confirmation message.
   */
  @PostMapping("/prepare-download")
  public ResponseEntity<String> prepareMediaObjectsForDownload(@RequestBody List<String> paths) {
    for (String path : paths) {
      MediaModel media = mediaRepository.findByPath(path);

      // Retrieve the associated library's storage class
      StorageClass storageClass = media.getLibrary().getStorageClass();
      prepareDownload(media, storageClass, jmsTemplate, mediaRepository);
    }

    return ResponseEntity.ok("Download preparation requests sent successfully");
  }

  /**
   * Cancels ongoing jobs for a list of media objects identified by their IDs.
   *
   * @param ids The IDs of the media objects for which jobs are to be cancelled.
   * @return A ResponseEntity with a confirmation message.
   */
  @PostMapping("/cancel-job")
  public ResponseEntity<String> cancelJobs(@RequestBody List<Long> ids) {
    for (long id : ids) {
      MediaModel media =
          mediaRepository
              .findById(id)
              .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + id));
      if (((media.isRecovering() && media.getDownloadSuccess() == null) || media.isArchiving())
          && !media.isJobCancelled()) {
        media.setJobCancelled(true);
        mediaRepository.save(media);
      }
    }

    return ResponseEntity.ok("Jobs cancelled");
  }

  /**
   * Cancels all ongoing archive jobs for media objects.
   *
   * @return A ResponseEntity with a confirmation message.
   */
  @PostMapping("/cancel-all-archive-jobs")
  public ResponseEntity<String> cancelAllArchiveJobs() {
    List<MediaModel> medias = mediaRepository.findByIsArchiving(true);
    for (MediaModel media : medias) {
      if (!media.isJobCancelled()) {
        media.setJobCancelled(true);
        mediaRepository.save(media);
      }
    }

    return ResponseEntity.ok("Jobs cancelled");
  }

  /**
   * Cancels all ongoing download jobs for media objects.
   *
   * @return A ResponseEntity with a confirmation message.
   */
  @PostMapping("/cancel-all-download-jobs")
  public ResponseEntity<String> cancelAllDownloadJobs() {
    List<MediaModel> medias = mediaRepository.findByIsRecovering(true);
    for (MediaModel media : medias) {
      if (!media.isJobCancelled() && media.getDownloadSuccess() == null) {
        media.setJobCancelled(true);
        mediaRepository.save(media);
      }
    }

    return ResponseEntity.ok("Jobs cancelled");
  }

  /**
   * Clears the status of all media objects that have finished downloading.
   *
   * @return A ResponseEntity with a confirmation message.
   */
  @PostMapping("/clear-all-finished")
  public ResponseEntity<String> clearAllFinishedDownloads() {
    List<MediaModel> successes = mediaRepository.findByDownloadSuccess(true);
    List<MediaModel> failures = mediaRepository.findByDownloadSuccess(false);

    for (MediaModel media : successes) {
      media.setDownloadSuccess(null);
      media.setRecovering(false);
      mediaRepository.save(media);
    }
    for (MediaModel media : failures) {
      media.setDownloadSuccess(null);
      media.setRecovering(false);
      mediaRepository.save(media);
    }
    return ResponseEntity.ok("Cleared finished downloads");
  }

  /**
   * Clears the status of selected media objects that have finished downloading.
   *
   * @param ids The IDs of the media objects to clear.
   * @return A ResponseEntity with a confirmation message.
   */
  @PostMapping("/clear-finished")
  public ResponseEntity<String> clearFinishedDownloads(@RequestBody List<Long> ids) {
    for (long id : ids) {
      MediaModel media =
          mediaRepository
              .findById(id)
              .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + id));
      if ((media.isRecovering() || media.isArchiving()) && !media.isJobCancelled()) {
        media.setDownloadSuccess(null);
        media.setRecovering(false);
        mediaRepository.save(media);
      }
    }
    return ResponseEntity.ok("Cleared finished selected downloads");
  }

  /**
   * Prepares a media object for download based on its storage class.
   *
   * @param media            The media object to prepare for download.
   * @param storageClass     The storage class of the media object's library.
   * @param jmsTemplate      The JmsTemplate for sending messages.
   * @param mediaRepository  The repository for saving changes to the media object.
   */
  public static void prepareDownload(
      MediaModel media,
      StorageClass storageClass,
      JmsTemplate jmsTemplate,
      MediaRepository mediaRepository) {
    if (!media.isRecovering() && media.getDownloadSuccess() == null && !media.isArchiving()) {
      media.setRecovering(true);
      mediaRepository.save(media);
      if (StorageClass.DEEP_ARCHIVE == storageClass || StorageClass.GLACIER == storageClass) {
        jmsTemplate.convertAndSend("restoreQueue", media.getPath());
      } else {
        jmsTemplate.convertAndSend("downloadQueue", media.getPath());
      }
    }
  }

  /**
   * Checks if a job related to a specific media object is cancelled.
   *
   * @param id The ID of the media object.
   * @return A Boolean indicating if the job is cancelled.
   * @throws ResourceNotFoundException If no media object is found with the given ID.
   */
  public Boolean getJobCancelled(@PathVariable Long id) {
    MediaModel media =
        mediaRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + id));
    return media.isJobCancelled();
  }
}
