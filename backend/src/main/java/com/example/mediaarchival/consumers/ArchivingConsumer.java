package com.example.mediaarchival.consumers;

import com.example.mediaarchival.controllers.MediaController;
import com.example.mediaarchival.models.LibraryModel;
import com.example.mediaarchival.models.MediaModel;
import com.example.mediaarchival.repositories.MediaRepository;
import com.example.mediaarchival.utils.DirectoryUtils;
import com.example.mediaarchival.utils.TarUtils;
import java.io.File;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedFileUpload;
import software.amazon.awssdk.transfer.s3.model.FileUpload;
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest;

/**
 * A consumer that processes archiving requests for media objects.
 * It listens to a JMS queue for paths of media objects to be archived,
 * creates a TAR archive, and uploads it to an S3 bucket.
 */

@Component
public class ArchivingConsumer {

  private final S3TransferManager transferManager;
  private final MediaRepository mediaRepository;

  private final MediaController mediaController;

  private static final Logger errorLogger = LoggerFactory.getLogger("ERROR_LOGGER");


  @Autowired
  public ArchivingConsumer(
      MediaRepository mediaRepository,
      S3TransferManager transferManager,
      MediaController mediaController) {
    this.transferManager = transferManager;
    this.mediaRepository = mediaRepository;
    this.mediaController = mediaController;
  }

  /**
   * Processes an archiving request from the queue.
   * @param path The path of the media object to be archived.
   */
  @JmsListener(
      destination = "archivingQueue",
      containerFactory = "containerFactory",
      concurrency = "5")
  public void processArchivingRequest(String path) {
    MediaModel media = mediaRepository.findByPath(path);
    LibraryModel library = media.getLibrary();
    if (mediaController.getJobCancelled(media.getId())) {
      updateMediaStatus(media);
      return;
    }
    try {
      File source = TarUtils.createTarArchive(media, mediaRepository);
      try {
        uploadToS3(library, source, media);
      } finally {
        source.delete();
      }
    } catch (CancellationException ignore) {
    } catch (Exception any) {
      errorLogger.error("error archiving media: " + any.getMessage());
      updateMediaStatus(media);
    }
  }

  /**
   * Uploads a file to an S3 bucket and handles the transfer process.
   * @param library The library model containing the bucket information.
   * @param source The source file to be uploaded.
   * @param media The media model associated with the file.
   */

  private void uploadToS3(LibraryModel library, File source, MediaModel media) {
    if (mediaController.getJobCancelled(media.getId())) {
      updateMediaStatus(media);
      return;
    }
    MediaObjectTransferListener listener =
        new MediaObjectTransferListener(
            mediaRepository, mediaController, media, 5, true, source.getPath());

    UploadFileRequest uploadFileRequest =
        UploadFileRequest.builder()
            .putObjectRequest(
                b ->
                    b.bucket(library.getBucketName())
                        .key(media.getPath())
                        .storageClass(library.getStorageClass()))
            .addTransferListener(listener)
            .source(source)
            .build();

    FileUpload fileUpload = transferManager.uploadFile(uploadFileRequest);
    CompletableFuture<CompletedFileUpload> future = fileUpload.completionFuture();

    listener.setFileUpload(fileUpload);

    future.join();
  }

  private void updateMediaStatus(MediaModel media) {
    media.setArchiving(false);
    media.setUploadProgress(-1);
    media.setTarring(false);
    media.setJobCancelled(false);
    mediaRepository.save(media);
  }
}
