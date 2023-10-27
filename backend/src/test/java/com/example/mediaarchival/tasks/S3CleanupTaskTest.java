package com.example.mediaarchival.tasks;

import com.example.mediaarchival.models.LibraryModel;
import com.example.mediaarchival.repositories.LibraryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
public class S3CleanupTaskTest {

    @Mock private S3Client s3Client;
    @Mock private LibraryRepository libraryRepository;
    @InjectMocks private S3CleanupTask s3CleanupTask;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        LibraryModel libraryModel = new LibraryModel();
        libraryModel.setBucketName("test-bucket");

        List<LibraryModel> libraries = Collections.singletonList(libraryModel);
        when(libraryRepository.findAll()).thenReturn(libraries);
    }

    @AfterEach
    public void tearDown() {
        reset(s3Client, libraryRepository);
    }

    @Test
    public void S3CleanupTask_whenOldUploadExists_thenAbortOldMultipartUploads() {
        MultipartUpload upload = MultipartUpload.builder()
                .key("old-file")
                .uploadId("upload-id")
                .initiated(Instant.now().minus(3, ChronoUnit.DAYS))
                .build();
        ListMultipartUploadsResponse response = ListMultipartUploadsResponse.builder()
                .uploads(Collections.singletonList(upload))
                .build();

        when(s3Client.listMultipartUploads(any(ListMultipartUploadsRequest.class)))
                .thenReturn(response);

        s3CleanupTask.abortOldMultipartUploads();

        verify(s3Client).listMultipartUploads(any(ListMultipartUploadsRequest.class));
        verify(s3Client).abortMultipartUpload(any(AbortMultipartUploadRequest.class));
    }

    @Test
    public void S3CleanupTask_whenNoUploadsExist_thenAbortOldMultipartUploadsNotCalled() {
        ListMultipartUploadsResponse response = ListMultipartUploadsResponse.builder()
                .uploads(Collections.emptyList())
                .build();

        when(s3Client.listMultipartUploads(any(ListMultipartUploadsRequest.class)))
                .thenReturn(response);

        s3CleanupTask.abortOldMultipartUploads();

        verify(s3Client).listMultipartUploads(any(ListMultipartUploadsRequest.class));
        verify(s3Client, never()).abortMultipartUpload(any(AbortMultipartUploadRequest.class));
    }
}
