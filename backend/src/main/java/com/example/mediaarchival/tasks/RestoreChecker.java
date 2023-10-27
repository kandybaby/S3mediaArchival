package com.example.mediaarchival.tasks;

import com.example.mediaarchival.controllers.MediaController;
import com.example.mediaarchival.models.MediaModel;
import com.example.mediaarchival.repositories.MediaRepository;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

/**
 * Scheduled task that checks the restore status of media items from S3 Glacier to S3.
 */
@Component
public class RestoreChecker {

  private final MediaRepository mediaRepository;
  private final S3Client s3Client;
  private final JmsTemplate jmsTemplate;
  private final MediaController mediaController;
  private static final Logger errorLogger = LoggerFactory.getLogger("ERROR_LOGGER");

  /**
   * Creates an instance of the RestoreChecker.
   *
   * @param mediaRepository Repository for media entities.
   * @param s3Client        The AWS S3 client for interacting with AWS S3.
   * @param jmsTemplate     JMS template for sending messages to the queue.
   * @param mediaController Controller for managing media-related operations.
   */
  @Autowired
  public RestoreChecker(
          MediaRepository mediaRepository,
          S3Client s3Client,
          JmsTemplate jmsTemplate,
          MediaController mediaController) {
    this.mediaRepository = mediaRepository;
    this.s3Client = s3Client;
    this.jmsTemplate = jmsTemplate;
    this.mediaController = mediaController;
  }

  /**
   * Periodically checks for the restoration status of media files that are being restored
   * from AWS S3 Glacier storage. If a media item's restoration is complete, it will send
   * a message to initiate a download process.
   */
  @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
  public void checkRestoreStatus() {
    try {
      List<MediaModel> inProgress = mediaRepository.findByIsRestoring(true);
      for (MediaModel media : inProgress) {
        checkAndHandleMedia(media);
      }
    } catch (Exception e) {
      errorLogger.error("Error retrieving in-progress media from the repository: " + e.getMessage());
    }
  }

  private void checkAndHandleMedia(MediaModel media) {
    try {
      if (!mediaController.getJobCancelled(media.getId())) {
        String bucket = media.getLibrary().getBucketName();
        String path = media.getPath();
        HeadObjectResponse response = s3Client.headObject(
                b -> b.bucket(bucket).key(path)
        );

        if (response.restore() != null && response.restore().contains("ongoing-request=\"false\"")) {
          updateMediaAndSendDownloadRequest(media);
        } else if (response.restore() == null){
          updateMediaAsNotRestored(media);
        }
      } else {
        cancelJob(media);
      }
    } catch (Exception e) {
      errorLogger.error("Error getting media data from S3" + media.getId() + ": " + e.getMessage());
    }
  }

  private void updateMediaAndSendDownloadRequest(MediaModel media) {
    try {
      mediaRepository.updateIsRestoringById(media.getId(), false);
      mediaRepository.updateIsRestoredById(media.getId(), true);
      jmsTemplate.convertAndSend("downloadQueue", media.getPath());
    } catch (Exception e) {
      errorLogger.error("Error updating media status or sending JMS message for media ID " + media.getId() + ": " + e.getMessage());
      updateMediaAsNotRestored(media);
      mediaRepository.updateIsRestoredById(media.getId(), true);
    }
  }

  private void updateMediaAsNotRestored(MediaModel media) {
    try {
      media.setRestored(false);
      media.setRecovering(true);
      media.setRestoring(false);
      media.setJobCancelled(false);
      media.setDownloadSuccess(false);
      media.setDownloadProgress(-1);
      mediaRepository.save(media);
    } catch (Exception e) {
      errorLogger.error("Error updating media as not restored for media ID " + media.getId() + ": " + e.getMessage());
    }
  }

  private void cancelJob(MediaModel media) {
    try {
      media.setRestored(false);
      media.setRecovering(false);
      media.setRestoring(false);
      media.setJobCancelled(false);
      media.setDownloadSuccess(null);
      media.setDownloadProgress(-1);
      mediaRepository.save(media);
    } catch (Exception e) {
      errorLogger.error("Error updating cancelled media for media ID " + media.getId() + ": " + e.getMessage());
    }
  }
}
