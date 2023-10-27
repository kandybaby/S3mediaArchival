package com.example.mediaarchival.tasks;

import com.example.mediaarchival.models.LibraryModel;
import com.example.mediaarchival.repositories.LibraryRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
/**
 * Class responsible for cleaning up old multipart uploads in S3 buckets.
 */
@Component
public class S3CleanupTask {

  private final S3Client s3Client;

  private final LibraryRepository libraryRepository;

  private static final Logger logger = LoggerFactory.getLogger(S3CleanupTask.class);

  @Autowired
  public S3CleanupTask(LibraryRepository libraryRepository, S3Client s3Client) {
    this.libraryRepository = libraryRepository;
    this.s3Client = s3Client;
  }

  /**
   * Aborts multipart uploads that are older than a specified cutoff time.
   * This method is scheduled to run every day at midnight.
   */
  @Scheduled(cron = "0 0 0 * * ?") // This cron expression means every day at midnight
  public void abortOldMultipartUploads() {
    List<LibraryModel> libraries = libraryRepository.findAll();
    for (LibraryModel library : libraries) {
      String bucketName = library.getBucketName();
      ListMultipartUploadsRequest listRequest =
          ListMultipartUploadsRequest.builder().bucket(bucketName).build();

      ListMultipartUploadsResponse listResponse = s3Client.listMultipartUploads(listRequest);
      List<MultipartUpload> uploads = listResponse.uploads();

      Instant cutoff = Instant.now().minus(java.time.Duration.ofDays(2));

      for (MultipartUpload upload : uploads) {
        if (upload.initiated().isBefore(cutoff)) {
          AbortMultipartUploadRequest abortRequest =
              AbortMultipartUploadRequest.builder()
                  .bucket(bucketName)
                  .key(upload.key())
                  .uploadId(upload.uploadId())
                  .build();

          s3Client.abortMultipartUpload(abortRequest);
          logger.info(
              "Aborted multipart upload for key: "
                  + upload.key()
                  + " with ID: "
                  + upload.uploadId());
        }
      }
    }
  }
}
