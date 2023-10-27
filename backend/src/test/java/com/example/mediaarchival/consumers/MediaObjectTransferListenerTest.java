package com.example.mediaarchival.consumers;

import static org.mockito.Mockito.*;

import com.example.mediaarchival.controllers.MediaController;
import com.example.mediaarchival.enums.ArchivedStatus;
import com.example.mediaarchival.models.MediaModel;
import com.example.mediaarchival.repositories.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.transfer.s3.progress.TransferListener;
import software.amazon.awssdk.transfer.s3.progress.TransferProgressSnapshot;

public class MediaObjectTransferListenerTest {

  @Mock private MediaRepository mediaRepository;

  @Mock private MediaController mediaController;

  @Mock private MediaModel mediaObject;
  @Mock private TransferListener.Context.TransferInitiated transferInitiatedContext;
  @Mock private TransferListener.Context.BytesTransferred bytesTransferredContext;
  @Mock private TransferListener.Context.TransferComplete transferCompleteContext;
  @Mock private TransferListener.Context.TransferFailed transferFailedContext;

  private MediaObjectTransferListener listener;

  private MediaModel mediaModel;
  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    mediaModel = new MediaModel();
    mediaModel.setId(1L);
    mediaModel.setSize(100L);
  }

  @Test
  void testTransferInitiated() {
    // Arrange
    listener =
            new MediaObjectTransferListener(
                    mediaRepository, mediaController, mediaModel, 10, true, "");

    when(mediaRepository.findById(anyLong())).thenReturn(java.util.Optional.of(mediaModel));

    // Act
    listener.transferInitiated(transferInitiatedContext);

    // Assert
    verify(mediaRepository).updateUploadProgressById(mediaModel.getId(), 0);
  }

  @Test
  void testBytesTransferred_Normal() {
    listener =
            new MediaObjectTransferListener(
                    mediaRepository, mediaController, mediaModel, 10, true, "");

    // Mock TransferProgressSnapshot
    TransferProgressSnapshot progressSnapshot = mock(TransferProgressSnapshot.class);
    when(progressSnapshot.transferredBytes()).thenReturn(50L);

    // Stub the method call
    when(bytesTransferredContext.progressSnapshot()).thenReturn(progressSnapshot);

    // Arrange
    when(mediaController.getJobCancelled(mediaModel.getId())).thenReturn(false);

    // Act
    listener.bytesTransferred(bytesTransferredContext);

    // Assert
    verify(mediaRepository).updateUploadProgressById(mediaModel.getId(), 50);
  }

  @Test
  void transferCompleteUpdatesMediaObject() {
    listener =
            new MediaObjectTransferListener(
                    mediaRepository, mediaController, mediaObject, 10, true, "");

    listener.transferComplete(transferCompleteContext);
    verify(mediaObject).setArchiving(false);
    verify(mediaObject).setUploadProgress(-1);
    verify(mediaObject).setArchivedStatus(ArchivedStatus.ARCHIVED);
    verify(mediaObject).setDateArchived(any());
    verify(mediaRepository).save(mediaObject);
  }

  @Test
  void transferFailedUpdatesMediaObject() {
    listener =
            new MediaObjectTransferListener(
                    mediaRepository, mediaController, mediaObject, 10, true, "");
    listener.transferFailed(transferFailedContext);
    verify(mediaObject).setArchiving(false);
    verify(mediaObject).setUploadProgress(-1);
    verify(mediaRepository).save(mediaObject);
  }
}
