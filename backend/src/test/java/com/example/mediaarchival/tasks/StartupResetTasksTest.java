package com.example.mediaarchival.tasks;

import com.example.mediaarchival.models.MediaModel;
import com.example.mediaarchival.repositories.MediaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StartupResetTasksTest {

    @Mock
    private MediaRepository mediaRepository;

    @InjectMocks
    private StartupResetTasks startupResetTasks;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() {
        reset(mediaRepository);
    }

    @Test
    void StartupResetTasks_whenResetArchivingStatusOnStartup_thenArchivingStatusIsReset() {
        MediaModel media1 = createTestMediaModel(true, false);
        MediaModel media2 = createTestMediaModel(true, false);
        List<MediaModel> inProgressMedia = Arrays.asList(media1, media2);

        when(mediaRepository.findByIsArchiving(true)).thenReturn(inProgressMedia);

        startupResetTasks.resetArchivingStatusOnStartup();

        verify(mediaRepository).findByIsArchiving(true);

        ArgumentCaptor<MediaModel> mediaModelArgumentCaptor = ArgumentCaptor.forClass(MediaModel.class);
        verify(mediaRepository, times(inProgressMedia.size())).save(mediaModelArgumentCaptor.capture());
        List<MediaModel> savedMedia = mediaModelArgumentCaptor.getAllValues();

        for (MediaModel media : savedMedia) {
            assertFalse(media.isArchiving());
            assertEquals(-1, media.getUploadProgress());
            assertFalse(media.isTarring());
            assertFalse(media.isJobCancelled());
        }
    }

    @Test
    void StartupResetTasks_whenResetRestorationProgressOnStartup_thenRestorationProgressIsReset() {
        MediaModel media1 = createTestMediaModel(false, true);
        MediaModel media2 = createTestMediaModel(false, true);
        List<MediaModel> inProgressMedia = Arrays.asList(media1, media2);

        when(mediaRepository.findByIsRecovering(true)).thenReturn(inProgressMedia);

        startupResetTasks.resetRestorationProgressOnStartup();

        verify(mediaRepository).findByIsRecovering(true);

        ArgumentCaptor<MediaModel> mediaModelArgumentCaptor = ArgumentCaptor.forClass(MediaModel.class);
        verify(mediaRepository, times(inProgressMedia.size())).save(mediaModelArgumentCaptor.capture());
        List<MediaModel> savedMedia = mediaModelArgumentCaptor.getAllValues();

        for (MediaModel media : savedMedia) {
            assertFalse(media.isRestored());
            assertFalse(media.isRecovering());
            assertFalse(media.isRestoring());
            assertEquals(-1, media.getDownloadProgress());
            assertNull(media.getDownloadSuccess());
            assertFalse(media.isJobCancelled());
        }
    }
    @Test
    void StartupResetTasks_whenResetArchivingStatusAndSaveFails_thenContinueWithNextMedia() {
        // Setup test data
        MediaModel media1 = createTestMediaModel(true, false);
        MediaModel media2 = createTestMediaModel(true, false);
        List<MediaModel> inProgressMedia = Arrays.asList(media1, media2);

        when(mediaRepository.findByIsArchiving(true)).thenReturn(inProgressMedia);

        doAnswer(invocation -> {
            MediaModel media = invocation.getArgument(0);
            if (media.equals(media1)) {
                throw new RuntimeException("Save failed");
            }
            return null;
        }).when(mediaRepository).save(any(MediaModel.class));

        startupResetTasks.resetArchivingStatusOnStartup();

        verify(mediaRepository).findByIsArchiving(true);

        ArgumentCaptor<MediaModel> mediaModelArgumentCaptor = ArgumentCaptor.forClass(MediaModel.class);
        verify(mediaRepository, times(2)).save(mediaModelArgumentCaptor.capture());

        List<MediaModel> savedMedia = mediaModelArgumentCaptor.getAllValues();

        assertEquals(media2, savedMedia.get(1));
        assertFalse(media2.isArchiving());
        assertEquals(-1, media2.getUploadProgress());
        assertFalse(media2.isTarring());
        assertFalse(media2.isJobCancelled());

        assertEquals(2, savedMedia.size());
    }


    private MediaModel createTestMediaModel(boolean isArchiving, boolean isRecovering) {
        MediaModel mediaModel = new MediaModel();
        mediaModel.setArchiving(isArchiving);
        mediaModel.setRecovering(isRecovering);
        mediaModel.setRestoring(false);
        return mediaModel;
    }
}
