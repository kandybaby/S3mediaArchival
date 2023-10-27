package com.example.mediaarchival.controllers;

import com.example.mediaarchival.enums.ArchivedStatus;
import com.example.mediaarchival.errors.ActiveJobsException;
import com.example.mediaarchival.errors.ResourceNotFoundException;
import com.example.mediaarchival.filters.MediaSpecifications;
import com.example.mediaarchival.models.LibraryModel;
import com.example.mediaarchival.models.MediaModel;
import com.example.mediaarchival.repositories.LibraryRepository;
import com.example.mediaarchival.repositories.MediaRepository;
import jakarta.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.StorageClass;


/**
 * Controller for managing library operations in the application.
 * This class provides endpoints for CRUD operations on libraries,
 * including creating, retrieving, and deleting libraries.
 * It also handles operations like scanning a library, archiving media objects
 * within a library, preparing media objects for download, and synchronizing media.
 *
 */
@RestController
@RequestMapping("/api/libraries")
public class LibraryController {
  private final LibraryRepository libraryRepository;
  private final MediaRepository mediaRepository;

  private final JmsTemplate jmsTemplate;

  @Autowired
  public LibraryController(
      LibraryRepository libraryRepository,
      MediaRepository mediaRepository,
      S3Client s3Client,
      JmsTemplate jmsTemplate) {
    this.libraryRepository = libraryRepository;
    this.mediaRepository = mediaRepository;
    this.jmsTemplate = jmsTemplate;
  }

  /**
   * Retrieves a list of all libraries.
   *
   * @return A ResponseEntity containing a list of all LibraryModel objects.
   */
  @GetMapping
  public ResponseEntity<List<LibraryModel>> getAllLibraries() {
    List<LibraryModel> libraries = libraryRepository.findAll();
    return ResponseEntity.ok(libraries);
  }

  /**
   * Retrieves a specific library by its ID.
   *
   * @param id The ID of the library to retrieve.
   * @return A ResponseEntity containing the requested LibraryModel.
   * @throws ResourceNotFoundException If no library is found with the given ID.
   */
  @GetMapping("/{id}")
  public ResponseEntity<LibraryModel> getLibraryById(@PathVariable Long id) {
    LibraryModel library =
        libraryRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Library not found with id: " + id));
    return ResponseEntity.ok(library);
  }

  /**
   * Creates a new library.
   *
   * @param library The LibraryModel object to be created.
   * @return A ResponseEntity containing the created LibraryModel or an error message.
   */
  @PostMapping
  public ResponseEntity<?> createLibrary(@RequestBody LibraryModel library) {
    // Check for existing library with the same name
    Optional<LibraryModel> existingLibraryByName = libraryRepository.findByName(library.getName());
    if (existingLibraryByName.isPresent()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Library with this name already exists.");
    }

    // Check for existing library with the same path
    Optional<LibraryModel> existingLibraryByPath = libraryRepository.findByPath(library.getPath());
    if (existingLibraryByPath.isPresent()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Library with this path already exists.");
    }

    LibraryModel savedLibrary = libraryRepository.save(library);
    return ResponseEntity.status(HttpStatus.CREATED).body(savedLibrary);
  }

  /**
   * Deletes a library identified by its ID.
   *
   * @param id The ID of the library to delete.
   * @return A ResponseEntity indicating the successful deletion.
   * @throws ResourceNotFoundException If no library is found with the given ID.
   */
  @DeleteMapping("/{id}")
  @Transactional
  public ResponseEntity<Void> deleteLibrary(@PathVariable Long id) {
    LibraryModel library =
        libraryRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Library not found with id: " + id));

    // Delete associated MediaModel objects
    List<MediaModel> mediaObjects = mediaRepository.findByLibraryId(id);
    mediaRepository.deleteAll(mediaObjects);

    libraryRepository.delete(library);
    return ResponseEntity.noContent().build();
  }

  /**
   * Initiates a scan of the library's media objects.
   *
   * @param id The ID of the library to scan.
   * @return A ResponseEntity indicating the initiation of the scan.
   * @throws ResourceNotFoundException If no library is found with the given ID.
   * @throws ActiveJobsException       If there are active jobs for the library.
   */
  @PostMapping("/{id}/scan")
  public ResponseEntity<Void> scanLibrary(@PathVariable Long id) {
    LibraryModel library =
        libraryRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Library not found with id: " + id));

    if (libraryHasActiveJobs(id)) {
      throw new ActiveJobsException(id);
    }

    library.setUpdating(true);
    libraryRepository.save(library);

    jmsTemplate.convertAndSend("libraryScanQueue", id);

    return ResponseEntity.noContent().build();
  }

  /**
   * Sends requests to archive all eligible media objects in a library.
   *
   * @param libraryId The ID of the library whose media objects are to be archived.
   * @return A ResponseEntity with a confirmation message.
   */
  @PostMapping("/{libraryId}/archive")
  public ResponseEntity<String> archiveLibraryMediaObjects(@PathVariable Long libraryId) {
    // Fetch media objects from the library with archivedStatus NOT_ARCHIVED or OUT_OF_DATE
    List<MediaModel> mediaObjects =
        mediaRepository.findByLibraryIdAndArchivedStatusIn(
            libraryId, Arrays.asList(ArchivedStatus.NOT_ARCHIVED, ArchivedStatus.OUT_OF_DATE));

    for (MediaModel media : mediaObjects) {
      if (!media.isArchiving() && !media.isRecovering()) {
        jmsTemplate.convertAndSend("archivingQueue", media.getPath());
        media.setArchiving(true);
        media.setUploadProgress(-1);
        media.setTarring(false);
        mediaRepository.save(media);
      }
    }

    return ResponseEntity.ok(
        "Archive requests sent successfully for eligible media objects in the library");
  }

  /**
   * Initiates synchronization of media objects in a library with its S3
   * bucket
   *
   * @param libraryId The ID of the library to synchronize.
   * @return A ResponseEntity indicating the initiation of synchronization.
   * @throws ResourceNotFoundException If no library is found with the given ID.
   * @throws ActiveJobsException       If there are active jobs for the library.
   */

  @PostMapping("/{libraryId}/synchronize")
  public ResponseEntity<String> synchronizeMedia(@PathVariable Long libraryId) {
    LibraryModel library =
        libraryRepository
            .findById(libraryId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Library with ID " + libraryId + " not found."));

    if (libraryHasActiveJobs(libraryId)) {
      throw new ActiveJobsException(libraryId);
    }

    library.setUpdating(true);
    libraryRepository.save(library);

    jmsTemplate.convertAndSend("librarySyncQueue", libraryId);

    return ResponseEntity.noContent().build();
  }

  private boolean libraryHasActiveJobs(long libraryId) {
    Specification<MediaModel> specification = MediaSpecifications.activeLibraryJobs(libraryId);
    List<MediaModel> activeJobs = mediaRepository.findAll(specification);
    return !activeJobs.isEmpty();
  }
}
