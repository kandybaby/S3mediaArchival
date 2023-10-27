package com.example.mediaarchival.consumers;

import static org.mockito.Mockito.*;

import com.example.mediaarchival.controllers.MediaController;
import com.example.mediaarchival.models.LibraryModel;
import com.example.mediaarchival.models.MediaModel;
import com.example.mediaarchival.repositories.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jms.core.JmsTemplate;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.RestoreObjectRequest;
import software.amazon.awssdk.services.s3.model.RestoreObjectResponse;

import java.util.Optional;

public class RestoreConsumerTest {

    @Mock private MediaRepository mediaRepository;
    @Mock private S3Client s3Client;
    @Mock private JmsTemplate jmsTemplate;
    @Mock private MediaController mediaController;

    @InjectMocks private RestoreConsumer restoreConsumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() {
        reset(mediaRepository, s3Client, jmsTemplate, mediaController);
    }

    @Test
    void testProcessRestoreRequest_AlreadyRestored() {
        // Arrange
        String testPath = "test/path";
        MediaModel mockMedia = new MediaModel();
        mockMedia.setId(1L);
        mockMedia.setPath(testPath);
        mockMedia.setLibrary(new LibraryModel());
        mockMedia.getLibrary().setBucketName("test-bucket");

        when(mediaRepository.findByPath(testPath)).thenReturn(mockMedia);
        when(mediaController.getJobCancelled(mockMedia.getId())).thenReturn(false);

        HeadObjectResponse mockResponse = HeadObjectResponse.builder()
                .restore("ongoing-request=\"false\"")
                .build();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(mockResponse);

        // Act
        restoreConsumer.processRestoreRequest(testPath);

        // Assert
        verify(jmsTemplate, times(1)).convertAndSend("downloadQueue", testPath);
    }

    @Test
    void testProcessRestoreRequest_RestoreInProgress() {
        // Arrange
        String testPath = "restore/path";
        MediaModel mockMedia = new MediaModel();
        mockMedia.setId(2L);
        mockMedia.setLibrary(new LibraryModel());
        mockMedia.getLibrary().setBucketName("test-bucket");

        when(mediaRepository.findByPath(testPath)).thenReturn(mockMedia);
        when(mediaController.getJobCancelled(mockMedia.getId())).thenReturn(false);

        HeadObjectResponse mockResponse = HeadObjectResponse.builder()
                .restore("ongoing-request=\"true\"")
                .build();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(mockResponse);

        // Act
        restoreConsumer.processRestoreRequest(testPath);

        // Assert
        verify(mediaRepository, times(1)).updateIsRestoringById(mockMedia.getId(), true);
    }

    @Test
    void testProcessRestoreRequest_StartRestore() {
        // Arrange
        String testPath = "start/restore";
        MediaModel mockMedia = new MediaModel();
        mockMedia.setId(3L);
        mockMedia.setPath(testPath);
        mockMedia.setLibrary(new LibraryModel());
        mockMedia.getLibrary().setBucketName("test-bucket");

        when(mediaRepository.findByPath(testPath)).thenReturn(mockMedia);
        when(mediaController.getJobCancelled(mockMedia.getId())).thenReturn(false);

        // Mock HeadObjectResponse to simulate that the object is not being restored
        HeadObjectResponse mockResponse = HeadObjectResponse.builder().restore(null).build();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(mockResponse);

        // Act
        restoreConsumer.processRestoreRequest(testPath);

        // Assert
        verify(s3Client, times(1)).restoreObject((RestoreObjectRequest) any());
        verify(mediaRepository, times(1)).updateIsRestoringById(mockMedia.getId(), true);
    }


    @Test
    void testProcessRestoreRequest_WithCancelledJob() {
        // Arrange
        String testPath = "cancelled/path";
        MediaModel mockMedia = new MediaModel();
        mockMedia.setId(4L);

        when(mediaRepository.findByPath(testPath)).thenReturn(mockMedia);
        when(mediaController.getJobCancelled(mockMedia.getId())).thenReturn(true);

        // Act
        restoreConsumer.processRestoreRequest(testPath);

        // Assert
        verify(jmsTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
        verify(mediaRepository, times(1)).save(mockMedia);
    }
}
