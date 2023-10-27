package com.example.mediaarchival.consumers;

import com.example.mediaarchival.controllers.MediaController;
import com.example.mediaarchival.models.MediaModel;
import com.example.mediaarchival.repositories.MediaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Component
public class RestoreConsumer {

  private final MediaRepository mediaRepository;
  private final S3Client s3Client;
  private final JmsTemplate jmsTemplate;
  private final MediaController mediaController;

  private static final Logger errorLogger = LoggerFactory.getLogger("ERROR_LOGGER");

  /**
   * Consumer responsible for processing media restoration requests.
   * It listens to messages on the 'restoreQueue' and handles the restoration
   * of media objects from archival storage.
   */
  @Autowired
  public RestoreConsumer(
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
   * Processes a restoration request for a specific media object.
   * This method is invoked with a path to the media that needs to be restored.
   *
   * @param path The path of the media object to restore.
   */
  @JmsListener(destination = "restoreQueue", containerFactory = "containerFactory")
  public void processRestoreRequest(String path) {
    MediaModel media = mediaRepository.findByPath(path);
    if (media == null) {
      errorLogger.error("Media not found for path: " + path);
      return;
    }

    try{
      boolean cancelled = mediaController.getJobCancelled(media.getId());
      if (!cancelled) {
        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(media.getLibrary().getBucketName())
                .key(media.getPath())
                .build();

        HeadObjectResponse response = s3Client.headObject(request);

        if (response.restore() != null && response.restore().contains("ongoing-request=\"false\"")) {
          jmsTemplate.convertAndSend("downloadQueue", media.getPath());
        } else if (response.restore() == null) {

          RestoreRequest restoreRequest = RestoreRequest.builder()
                  .days(3)
                  .glacierJobParameters(GlacierJobParameters.builder().tier(Tier.BULK).build())
                  .build();

          RestoreObjectRequest objectRequest = RestoreObjectRequest.builder()
                  .bucket(media.getLibrary().getBucketName())
                  .key(media.getPath())
                  .restoreRequest(restoreRequest)
                  .build();

          s3Client.restoreObject(objectRequest);

          mediaRepository.updateIsRestoringById(media.getId(), true);
        } else {
          mediaRepository.updateIsRestoringById(media.getId(), true);
        }
      } else {
        media.setRestored(false);
        media.setRecovering(false);
        media.setRestoring(false);
        media.setJobCancelled(false);
        media.setDownloadProgress(-1);
        mediaRepository.save(media);
      }
    } catch (Exception e){
      errorLogger.error(e.getMessage());
      media.setRestored(false);
      media.setRecovering(false);
      media.setRestoring(false);
      media.setJobCancelled(false);
      media.setDownloadProgress(-1);
      mediaRepository.save(media);
    }
  }
}
