package com.example.mediaarchival.consumers;

import com.example.mediaarchival.enums.ArchivedStatus;
import com.example.mediaarchival.enums.MediaCategory;
import com.example.mediaarchival.models.LibraryModel;
import com.example.mediaarchival.models.MediaModel;
import com.example.mediaarchival.repositories.LibraryRepository;
import com.example.mediaarchival.repositories.MediaRepository;
import com.example.mediaarchival.utils.DirectoryUtils;
import java.io.File;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

@Component
public class LibraryUpdateConsumer {

  private final LibraryRepository libraryRepository;
  private final MediaRepository mediaRepository;

  private final S3Client s3Client;

  private static final Logger errorLogger = LoggerFactory.getLogger("ERROR_LOGGER");

  /**
   * Consumer class responsible for handling library update operations.
   * It listens to JMS queues for library scan and synchronization requests.
   */
  @Autowired
  public LibraryUpdateConsumer(
      LibraryRepository libraryRepository, MediaRepository mediaRepository, S3Client s3Client) {
    this.libraryRepository = libraryRepository;
    this.mediaRepository = mediaRepository;
    this.s3Client = s3Client;
  }

  /**
   * Scans a library and updates media objects based on the local file system.
   * Triggered by messages from the 'libraryScanQueue'.
   *
   * @param libraryId The ID of the library to be scanned.
   */
  @JmsListener(
      destination = "libraryScanQueue",
      containerFactory = "containerFactory",
      concurrency = "1")
  public void scanLibrary(Long libraryId) {
    LibraryModel library = null;
    Optional<LibraryModel> libraryOptional;

    try {
      libraryOptional = libraryRepository.findById(libraryId);
      if (libraryOptional.isEmpty()) {
        return;
      }
      library = libraryOptional.get();
      String libraryPath = library.getPath(); // Get the library path
      String[] segments = libraryPath.split("/");
      if (libraryPath.charAt(0) == '/') {
        segments[0] = "/";
      }
      scanDirectory(segments[0], segments, library);
    } catch(Exception e){
      errorLogger.error(e.getMessage());
    } finally {
      if(library != null){
        library.setUpdating(false);
        libraryRepository.save(library);
      }
    }
  }

  /**
   * Synchronizes a library with its corresponding objects in the S3 bucket.
   * Triggered by messages from the 'librarySyncQueue'.
   *
   * @param libraryId The ID of the library to be synchronized.
   */
  @JmsListener(
      destination = "librarySyncQueue",
      containerFactory = "containerFactory",
      concurrency = "1")
  public void synchronizeLibrary(Long libraryId) {
    LibraryModel library = null;
    Optional<LibraryModel> libraryOptional;
    try {
      libraryOptional = libraryRepository.findById(libraryId);
      if (libraryOptional.isEmpty()) {
        return;
      }
      library = libraryOptional.get();

      String bucketName = library.getBucketName();

      boolean moreObjectsExist = true;
      String continuationToken = null;

      while (moreObjectsExist) {
        ListObjectsV2Request request =
                ListObjectsV2Request.builder() // Using the builder pattern here
                        .bucket(bucketName)
                        .continuationToken(continuationToken)
                        .build();

        ListObjectsV2Response result = this.s3Client.listObjectsV2(request);
        for (software.amazon.awssdk.services.s3.model.S3Object object : result.contents()) {
          String modifiedPath = object.key();
          MediaModel media = mediaRepository.findByPath(modifiedPath);
          if (media != null) {
            Instant s3LastModifiedDate = object.lastModified();
            Instant mediaLastModifiedDate = media.getDateLastModified();

            if (mediaLastModifiedDate.isAfter(s3LastModifiedDate)) {
              media.setArchivedStatus(ArchivedStatus.OUT_OF_DATE);
            } else {
              media.setArchivedStatus(ArchivedStatus.ARCHIVED);
            }

            media.setDateArchived(object.lastModified());
            mediaRepository.save(media);
          }
        }

        if (result.isTruncated()) {
          continuationToken = result.nextContinuationToken();
        } else {
          moreObjectsExist = false;
        }
      }
    } catch(Exception e){
      errorLogger.error(e.getMessage());
    } finally {
      if(library != null){
        library.setUpdating(false);
        libraryRepository.save(library);
      }
    }
  }

  private void scanDirectory(String currentPath, String[] segments, LibraryModel library) {
    File currentDir = new File(currentPath);

    // Check if the current directory exists
    if (!currentDir.exists() || !currentDir.isDirectory()) {
      return;
    }

    // If there are no more segments left, create/update media objects for all files and directories
    if (segments.length == 1) {
      File[] filesAndDirs = currentDir.listFiles();
      if (filesAndDirs != null) {
        for (File fileOrDir : filesAndDirs) {
          createOrUpdateMediaObject(fileOrDir, library);
        }
      }
      return;
    }

    // Get the next segment
    String nextSegment = segments[1];
    String[] remainingSegments = Arrays.copyOfRange(segments, 1, segments.length);

    if (nextSegment.startsWith("${") && nextSegment.endsWith("}")) {
      File[] subDirs = currentDir.listFiles(File::isDirectory);
      if (subDirs != null) {
        for (File subDir : subDirs) {
          scanDirectory(currentPath + "/" + subDir.getName(), remainingSegments, library);
        }
      }
    } else {
      scanDirectory(currentPath + "/" + nextSegment, remainingSegments, library);
    }
  }

  // Helper method to create or update a MediaModel object
  private void createOrUpdateMediaObject(File file, LibraryModel library) {
    Instant lastModified = Instant.ofEpochMilli(file.lastModified());
    // Check if a media object with the same path already exists
    MediaModel existingMedia;
    if (library.getCategory() == MediaCategory.TV) {
      existingMedia = mediaRepository.findByPath(file.getPath() + "/metadata");
    } else {
      existingMedia = mediaRepository.findByPath(file.getPath());
    }

    if (existingMedia != null) {
      // Check if the existing object is ARCHIVED and out of date
      if (existingMedia.getArchivedStatus() == ArchivedStatus.ARCHIVED
          && existingMedia.getDateArchived() != null
          && lastModified.isAfter(existingMedia.getDateArchived())) {
        existingMedia.setArchivedStatus(ArchivedStatus.OUT_OF_DATE);
        existingMedia.setSize(
            DirectoryUtils.getDirectorySize(file, library.getCategory() == MediaCategory.TV));

        existingMedia.setSize(file.getTotalSpace());
      }
      existingMedia.setDateLastModified(lastModified);
      mediaRepository.save(existingMedia);
    } else {
      MediaModel media = new MediaModel();
      media.setLibrary(library);
      media.setArchivedStatus(ArchivedStatus.NOT_ARCHIVED);
      media.setDateLastModified(lastModified);
      media.setArchiving(false);
      media.setUploadProgress(-1);
      media.setTarring(false);
      media.setRestoring(false);
      media.setRestored(false);
      media.setDownloadProgress(-1);
      media.setJobCancelled(false);

      if (library.getCategory() == MediaCategory.TV) {
        media.setName(file.getName() + " metadata");
        media.setPath(file.getPath() + "/metadata");
        media.setSize(DirectoryUtils.getDirectorySize(file, true));
      } else {
        media.setName(file.getName());
        media.setPath(file.getPath());
        media.setSize(DirectoryUtils.getDirectorySize(file, false));
      }

      // Save the media object
      mediaRepository.save(media);
    }

    // Check if the library's mediaCategory is TV
    if (library.getCategory() == MediaCategory.TV) {
      if (file.isDirectory()) {
        File[] subDirs = file.listFiles(File::isDirectory);
        if (subDirs != null) {
          for (File subDir : subDirs) {
            // If the subdirectory contains "season" in its name, save it
            if (subDir.getName().toLowerCase().contains("season")) {
              // Check if a TV season media object with the same path already exists
              String seasonPath = file.getPath() + "/" + subDir.getName();
              MediaModel existingSeasonMedia = mediaRepository.findByPath(seasonPath);
              if (existingSeasonMedia != null) {
                // Update the existing TV season media object
                existingSeasonMedia.setDateLastModified(lastModified);

                // Check if the existing TV season object is ARCHIVED and out of date
                if (existingSeasonMedia.getArchivedStatus() == ArchivedStatus.ARCHIVED
                    && existingSeasonMedia.getDateArchived() != null
                    && lastModified.isAfter(existingSeasonMedia.getDateArchived())) {
                  existingSeasonMedia.setArchivedStatus(ArchivedStatus.OUT_OF_DATE);
                  existingSeasonMedia.setSize(DirectoryUtils.getDirectorySize(subDir, false));
                }
                // Save the updated TV season media object
                mediaRepository.save(existingSeasonMedia);
              } else {
                // Save the TV season directory as a new media object
                MediaModel seasonMedia = new MediaModel();
                seasonMedia.setName(file.getName() + " " + subDir.getName());
                seasonMedia.setPath(seasonPath);
                seasonMedia.setLibrary(library);
                seasonMedia.setDateLastModified(lastModified);
                seasonMedia.setArchivedStatus(ArchivedStatus.NOT_ARCHIVED);
                seasonMedia.setArchiving(false);
                seasonMedia.setUploadProgress(-1);
                seasonMedia.setTarring(false);
                seasonMedia.setRestoring(false);
                seasonMedia.setRestored(false);
                seasonMedia.setDownloadProgress(-1);
                seasonMedia.setJobCancelled(false);
                seasonMedia.setSize(DirectoryUtils.getDirectorySize(subDir, false));

                // Save the TV season media object
                mediaRepository.save(seasonMedia);
              }
            }
          }
        }
      }
    }
  }
}
