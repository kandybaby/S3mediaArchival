package com.example.mediaarchival.consumers;

import com.example.mediaarchival.controllers.MediaController;
import com.example.mediaarchival.enums.ArchivedStatus;
import com.example.mediaarchival.models.MediaModel;
import com.example.mediaarchival.repositories.MediaRepository;
import com.example.mediaarchival.utils.TarUtils;
import java.io.File;
import java.io.IOException;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.transfer.s3.model.FileDownload;
import software.amazon.awssdk.transfer.s3.model.FileUpload;
import software.amazon.awssdk.transfer.s3.progress.TransferListener;

public class MediaObjectTransferListener implements TransferListener {

  private final MediaRepository mediaRepository;
  private final MediaController mediaController;
  private final MediaModel mediaObject;
  private final int updateIntervalPercentage;

  private final String tempPath;

  private final boolean isUpload;

  private long lastTransferredBytes = 0;
  private FileUpload fileUpload;
  private FileDownload fileDownload;
  private boolean jobPaused = false;

  private static final Logger errorLogger = LoggerFactory.getLogger("ERROR_LOGGER");

  /**
   * A listener for media object transfer events, handling the progress tracking and completion
   * status updates for both uploads and downloads of media objects.
   */

  public MediaObjectTransferListener(
          MediaRepository mediaRepository,
          MediaController mediaController,
          MediaModel mediaObject,
          int updateIntervalPercentage,
          boolean isUpload,
          String tempPath) {
    this.mediaRepository = mediaRepository;
    this.mediaController = mediaController;
    this.mediaObject = mediaObject;
    this.updateIntervalPercentage = updateIntervalPercentage;
    this.tempPath = tempPath;
    this.isUpload = isUpload;
  }

  /**
   * Handles the initiation of a media object transfer.
   *
   * @param context The context of the transfer initiation.
   */
  @Override
  public void transferInitiated(TransferListener.Context.TransferInitiated context) {
    setProgress(mediaObject, 0);
  }

  /**
   * Handles the bytes transferred event during a media object transfer.
   * Updates the progress of the transfer in the database based on the bytes transferred.
   *
   * @param context The context containing the details of the transfer progress.
   */

  @Override
  public void bytesTransferred(TransferListener.Context.BytesTransferred context) {
    long transferredBytes = context.progressSnapshot().transferredBytes();
    long size = mediaObject.getSize();
    if (mediaController.getJobCancelled(mediaObject.getId()) && !jobPaused) {
      if (fileUpload != null && !fileUpload.completionFuture().isDone()) {
        jobPaused = true;
        fileUpload.pause();
      }
      if (fileDownload != null && !fileDownload.completionFuture().isDone()) {
        jobPaused = true;
        fileDownload.pause();
      }
      return;
    }
    // Calculate the percentage of completion
    int progressPercentage = (int) ((transferredBytes * 100) / size);

    if (progressPercentage - lastTransferredBytes >= updateIntervalPercentage) {
      setProgress(mediaObject, progressPercentage);
      lastTransferredBytes = progressPercentage;
    }
  }

  /**
   * Handles the completion of a media object transfer.
   * Performs different actions based on whether it was an upload or download.
   *
   * @param context The context of the transfer completion.
   */

  @Override
  public void transferComplete(TransferListener.Context.TransferComplete context) {
    if (this.isUpload) {
      successUpload();
    } else {
      try {
        successDownload();
      } catch (IOException e) {
        mediaObject.setDownloadSuccess(false);
        mediaRepository.save(mediaObject);
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Handles the failure of a media object transfer.
   * Updates the media object's status in the database and logs the failure.
   *
   * @param context The context containing details of the transfer failure.
   */

  @Override
  public void transferFailed(TransferListener.Context.TransferFailed context) {
    if (this.isUpload) {
      failedUpload();
    } else {
      failedDownload();
    }
  }

  /**
   * Sets the FileUpload instance for this listener.
   *
   * @param fileUpload The FileUpload instance.
   */

  public void setFileUpload(FileUpload fileUpload) {
    this.fileUpload = fileUpload;
  }

  /**
   * Sets the FileDownload instance for this listener.
   *
   * @param fileDownload The FileUpload/FileDownload instance.
   */
  public void setFileDownload(FileDownload fileDownload) {
    this.fileDownload = fileDownload;
  }

  private void failedUpload() {
    mediaObject.setArchiving(false);
    mediaObject.setUploadProgress(-1);
    mediaRepository.save(mediaObject);
  }

  private void successUpload() {
    mediaObject.setArchiving(false);
    mediaObject.setUploadProgress(-1);
    mediaObject.setArchivedStatus(ArchivedStatus.ARCHIVED);
    mediaObject.setDateArchived(Instant.now());
    mediaRepository.save(mediaObject);
  }

  private void failedDownload() {
    mediaObject.setDownloadProgress(-1);
    mediaObject.setRestored(false);
    mediaObject.setRestoring(false);
    mediaObject.setRecovering(true);
    mediaObject.setDownloadSuccess(false);
    mediaRepository.save(mediaObject);
    try {
      File tar = new File(this.tempPath);
      tar.delete();
    } catch (Exception e) {
     errorLogger.error("Error deleting failed download: " + e.getMessage());
    }
  }

  private void successDownload() throws IOException {
    mediaObject.setRestoring(false);
    mediaObject.setRestored(false);
    mediaRepository.save(mediaObject);
    File tar = new File(this.tempPath);
    TarUtils.unpackTarArchive(tar);
    mediaObject.setDownloadSuccess(true);
    mediaObject.setRecovering(true);
    mediaRepository.save(mediaObject);
  }

  private void setProgress(MediaModel media, int progress) {
    if (this.isUpload) {
      mediaRepository.updateUploadProgressById(media.getId(), progress);
    } else {
      mediaRepository.updateDownloadProgressById(media.getId(), progress);
    }
  }
}