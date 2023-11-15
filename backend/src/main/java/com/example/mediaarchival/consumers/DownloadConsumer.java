package com.example.mediaarchival.consumers;

import com.example.mediaarchival.controllers.MediaController;
import com.example.mediaarchival.models.LibraryModel;
import com.example.mediaarchival.models.MediaModel;
import com.example.mediaarchival.repositories.MediaRepository;
import com.example.mediaarchival.utils.DirectoryUtils;
import com.example.mediaarchival.utils.EnvUtils;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedFileDownload;
import software.amazon.awssdk.transfer.s3.model.DownloadFileRequest;
import software.amazon.awssdk.transfer.s3.model.FileDownload;

/**
 * Consumer class that handles the downloading of media objects from an S3 bucket.
 * This class listens to a JMS queue for download requests and processes them accordingly.
 */
@Component
public class DownloadConsumer {

  private final S3TransferManager transferManager;
  private final MediaRepository mediaRepository;

  private final MediaController mediaController;
  private static final Logger errorLogger = LoggerFactory.getLogger("ERROR_LOGGER");
  @Autowired
  public DownloadConsumer(
      MediaRepository mediaRepository,
      S3TransferManager transferManager,
      MediaController mediaController) {
    this.transferManager = transferManager;
    this.mediaRepository = mediaRepository;
    this.mediaController = mediaController;
  }

  /**
   * Downloads a media object from an S3 bucket based on a path received from the download queue.
   * This method listens to a JMS queue and triggers when a download request is received.
   *
   * @param path The path of the media object in the S3 bucket.
   * @throws IOException If an I/O error occurs during the download process.
   */
  @JmsListener(
      destination = "downloadQueue",
      containerFactory = "containerFactory",
      concurrency = "5")
  public void downloadObject(String path) throws IOException {
    MediaModel media = mediaRepository.findByPath(path);
    boolean cancelled = mediaController.getJobCancelled(media.getId());
    if (!cancelled) {
      try {
        LibraryModel library = media.getLibrary();
        String downloadPath = EnvUtils.getDownloadDirectory() + "/" + media.getPath() + ".tar";
        DirectoryUtils.createDirectoriesExceptLast(downloadPath);

        MediaObjectTransferListener listener =
            new MediaObjectTransferListener(
                mediaRepository, mediaController, media, 5, false, downloadPath);

        DownloadFileRequest downloadFileRequest =
            DownloadFileRequest.builder()
                .getObjectRequest(
                    req ->
                        req.bucket(library.getBucketName()).key(path))
                .destination(Paths.get(downloadPath))
                .addTransferListener(listener)
                .build();

        FileDownload fileDownload = transferManager.downloadFile(downloadFileRequest);

        CompletableFuture<CompletedFileDownload> future = fileDownload.completionFuture();

        listener.setFileDownload(fileDownload);

        future.join();
      } catch (CancellationException ignore) {
      } catch (Exception any) {
        errorLogger.error("error downloading media: " + any.getMessage());
      } finally {
        resetMedia(media);
        media.setDownloadSuccess(false);
        mediaRepository.save(media);
      }
    } else {
      resetMedia(media);
      mediaRepository.updateIsRecoveringById(media.getId(), false);
    }
  }

  private void resetMedia(MediaModel media) {
    media.setDownloadProgress(-1);
    media.setRestored(false);
    media.setRestoring(false);
    media.setDownloadProgress(-1);
    media.setJobCancelled(false);
    mediaRepository.save(media);
  }
}
