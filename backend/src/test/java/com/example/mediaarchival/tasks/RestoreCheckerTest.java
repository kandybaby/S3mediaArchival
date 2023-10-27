package com.example.mediaarchival.tasks;

import static org.mockito.Mockito.*;

import com.example.mediaarchival.controllers.MediaController;
import com.example.mediaarchival.models.LibraryModel;
import com.example.mediaarchival.models.MediaModel;
import com.example.mediaarchival.repositories.MediaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jms.core.JmsTemplate;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.Collections;
import java.util.List;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;


public class RestoreCheckerTest {

    @Mock private MediaRepository mediaRepository;
    @Mock private S3Client s3Client;
    @Mock private JmsTemplate jmsTemplate;
    @Mock private MediaController mediaController;
    @InjectMocks private RestoreChecker restoreChecker;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @AfterEach
    public void tearDown() {
        reset(mediaRepository, s3Client, jmsTemplate, mediaController);
    }


    @Test
    public void checkRestoreStatus_MediaIsRestored_SendDownloadMessage() {
        LibraryModel libraryModel = new LibraryModel();
        libraryModel.setBucketName("bucket-name");

        MediaModel mediaModel = new MediaModel();
        mediaModel.setRestored(false);
        mediaModel.setId(1L); // Assuming the ID is a Long
        mediaModel.setLibrary(libraryModel);
        mediaModel.setPath("media-path");

        List<MediaModel> mediaList = Collections.singletonList(mediaModel);
        when(mediaRepository.findByIsRestoring(true)).thenReturn(mediaList);

        // Capture the Consumer passed to the headObject method
        ArgumentCaptor<Consumer<HeadObjectRequest.Builder>> argumentCaptor = ArgumentCaptor.forClass(Consumer.class);

        // Assume headObject is being called with a lambda that configures the builder
        when(s3Client.headObject(argumentCaptor.capture())).thenAnswer(invocation -> {
            HeadObjectRequest.Builder builder = HeadObjectRequest.builder();
            argumentCaptor.getValue().accept(builder);
            HeadObjectRequest request = builder.build();

            // Now you can check if the request has the correct bucket and key
            Assertions.assertEquals("bucket-name", request.bucket());
            Assertions.assertEquals("media-path", request.key());

            // Return a mock response
            return HeadObjectResponse.builder().restore("ongoing-request=\"false\"").build();
        });


        // Execute
        restoreChecker.checkRestoreStatus();

        // Verify
        verify(jmsTemplate).convertAndSend(eq("downloadQueue"), eq("media-path"));
    }

    @Test
    public void checkRestoreStatus_JobCancelled_DoNotUpdateOrSendDownload() {
        MediaModel mediaModel = createTestMediaModel();
        mediaModel.setJobCancelled(true);

        List<MediaModel> mediaList = Collections.singletonList(mediaModel);
        when(mediaRepository.findByIsRestoring(true)).thenReturn(mediaList);
        when(mediaController.getJobCancelled(mediaModel.getId())).thenReturn(true);

        restoreChecker.checkRestoreStatus();

        // Since the job is cancelled, no further actions should be taken.
        verify(mediaRepository, never()).updateIsRestoringById(any(Long.class), anyBoolean());
        verify(jmsTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
    }

    @Test
    public void checkRestoreStatus_MediaAlreadyRestored_DoNotUpdateOrSendDownload() {
        MediaModel mediaModel = createTestMediaModel();
        mediaModel.setRestored(true);

        List<MediaModel> mediaList = Collections.singletonList(mediaModel);
        when(mediaRepository.findByIsRestoring(true)).thenReturn(mediaList);

        restoreChecker.checkRestoreStatus();

        verify(mediaRepository, never()).updateIsRestoringById(anyLong(), anyBoolean());
        verify(jmsTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
    }
    @Test
    public void checkRestoreStatus_RestoreInProgress_DoNotUpdateOrSendDownload() {
        MediaModel mediaModel = createTestMediaModel();

        List<MediaModel> mediaList = Collections.singletonList(mediaModel);
        when(mediaRepository.findByIsRestoring(true)).thenReturn(mediaList);
        when(s3Client.headObject(any(Consumer.class)))
                .thenReturn(HeadObjectResponse.builder().restore("ongoing-request=\"true\"").build());

        restoreChecker.checkRestoreStatus();

        verify(mediaRepository, never()).updateIsRestoringById(anyLong(), anyBoolean());
        verify(jmsTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
    }

    @Test
    public void checkRestoreStatus_RestoreNotStarted_UpdateMediaAsNotRestored() {
        MediaModel mediaModel = createTestMediaModel();

        List<MediaModel> mediaList = Collections.singletonList(mediaModel);
        when(mediaRepository.findByIsRestoring(true)).thenReturn(mediaList);
        when(s3Client.headObject(any(Consumer.class)))
                .thenReturn(HeadObjectResponse.builder().restore(null).build());

        restoreChecker.checkRestoreStatus();

        verify(mediaRepository, never()).updateIsRestoringById(anyLong(), anyBoolean());
        verify(jmsTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
        verify(mediaRepository).save(any(MediaModel.class));
    }


    private MediaModel createTestMediaModel() {
        LibraryModel libraryModel = new LibraryModel();
        libraryModel.setBucketName("bucket-name");

        MediaModel mediaModel = new MediaModel();
        mediaModel.setRestored(false);
        mediaModel.setId(1L);
        mediaModel.setLibrary(libraryModel);
        mediaModel.setPath("media-path");
        return mediaModel;
    }

}
