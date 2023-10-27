package com.example.mediaarchival.tasks;

import com.example.mediaarchival.models.MediaModel;
import com.example.mediaarchival.repositories.MediaRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Component that performs reset tasks on application startup.
 * This effectively removes any in progress jobs that happened
 * during an application shut down or crash. Media objects being restored
 * to S3 from glacier have their jobs re-added.
 */
@Component
public class StartupResetTasks {

  @Autowired
  private MediaRepository mediaRepository;

  private static final Logger errorLogger = LoggerFactory.getLogger("ERROR_LOGGER");

  /**
   * Resets the archiving status for all media marked as archiving upon startup.
   * Each media is processed independently to ensure that a failure in one does not affect the others.
   */
  @PostConstruct
  public void resetArchivingStatusOnStartup() {
    List<MediaModel> inProgressMedia = mediaRepository.findByIsArchiving(true);
    for (MediaModel media : inProgressMedia) {
      try {
        media.setArchiving(false);
        media.setUploadProgress(-1);
        media.setTarring(false);
        media.setJobCancelled(false);
        mediaRepository.save(media);
      } catch (Exception e) {
        errorLogger.error("Error clearing media job during startup " + media.getId() + ": " + e.getMessage());
      }
    }
  }

  /**
   * Resets the restoration progress for all media marked as recovering on startup
   * unless they are currently restoring. Processed independently to handle failures per media.
   */
  @PostConstruct
  public void resetRestorationProgressOnStartup() {
    List<MediaModel> inProgressMedia = mediaRepository.findByIsRecovering(true);
    for (MediaModel media : inProgressMedia) {
      if (!media.isRestoring()) {
        try {
          media.setRestored(false);
          media.setRecovering(false);
          media.setRestoring(false);
          media.setDownloadProgress(-1);
          media.setDownloadSuccess(null);
          media.setJobCancelled(false);
          mediaRepository.save(media);
        } catch (Exception e) {
          errorLogger.error("Error clearing media job during startup " + media.getId() + ": " + e.getMessage());
        }
      }
    }
  }
}
