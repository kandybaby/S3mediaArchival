package com.example.mediaarchival.consumers;

import com.example.mediaarchival.controllers.LibraryController;
import com.example.mediaarchival.enums.ArchivedStatus;
import com.example.mediaarchival.enums.MediaCategory;
import com.example.mediaarchival.models.LibraryModel;
import com.example.mediaarchival.models.MediaModel;
import com.example.mediaarchival.repositories.LibraryRepository;
import com.example.mediaarchival.repositories.MediaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

public class LibraryUpdateConsumerTest {

    @Mock
    private LibraryRepository libraryRepository;

    @Mock private MediaRepository mediaRepository;

    @Mock private S3Client s3Client;

    @InjectMocks
    private LibraryUpdateConsumer libraryUpdateConsumer;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void tearDown() {
        reset(mediaRepository, libraryRepository);
    }

    @Test
    public void testScanLibraryMusic() throws Exception {
        // Arrange
        Long libraryId = 1L;
        LibraryModel library = new LibraryModel();
        library.setName("Test Library");
        library.setCategory(MediaCategory.OTHER);
        library.setPath("testVolume/music/${artist}/");
        library.setId(libraryId);
        when(libraryRepository.findById(libraryId)).thenReturn(Optional.of(library));

        List<String> expectedMediaObjectPaths = new ArrayList<>();
        expectedMediaObjectPaths.add("testVolume/music/Carly Rae Jepsen/The Loveliest Time");
        expectedMediaObjectPaths.add(
                "testVolume/music/Chapell Roan/The Rise and Fall of a Midwest Princess");

        // Mock the behavior of mediaRepository without hitting the database
        when(mediaRepository.save(any(MediaModel.class))).thenReturn(null); // Adjust this as needed

        // Act
        libraryUpdateConsumer.scanLibrary(1L);

        // Assert
        verify(mediaRepository, times(2)).save(any(MediaModel.class));
        for (String expectedPath : expectedMediaObjectPaths) {
            verify(mediaRepository).save(argThat(media -> media.getPath().equals(expectedPath)));
        }
    }

    @Test
    public void testScanLibraryMusicWithExistingMedia() throws Exception {
        // Arrange
        Long libraryId = 1L;
        LibraryModel library = new LibraryModel();
        library.setId(libraryId);
        library.setName("Test Library");
        library.setCategory(MediaCategory.OTHER);
        library.setPath("testVolume/music/${artist}/");
        when(libraryRepository.findById(libraryId)).thenReturn(Optional.of(library));

        // Mock the behavior of mediaRepository without hitting the database
        when(mediaRepository.save(any(MediaModel.class))).thenReturn(null); // Adjust this as needed

        // Mock existing media objects
        MediaModel existingMedia1 = new MediaModel();
        existingMedia1.setId(1L);
        existingMedia1.setPath("testVolume/music/Carly Rae Jepsen/The Loveliest Time");
        existingMedia1.setArchivedStatus(ArchivedStatus.ARCHIVED);
        existingMedia1.setDateArchived(Instant.MIN); // Use the earliest possible date
        when(mediaRepository.findByPath(eq(existingMedia1.getPath()))).thenReturn(existingMedia1);

        MediaModel existingMedia2 = new MediaModel();
        existingMedia2.setId(2L);
        existingMedia2.setPath("testVolume/music/Chapell Roan/The Rise and Fall of a Midwest Princess");
        existingMedia2.setArchivedStatus(ArchivedStatus.ARCHIVED);
        existingMedia2.setDateArchived(Instant.now());
        when(mediaRepository.findByPath(eq(existingMedia2.getPath()))).thenReturn(existingMedia2);

        // Act
        libraryUpdateConsumer.scanLibrary(1L);

        // Assert
        verify(mediaRepository, times(2)).save(any(MediaModel.class));

        // Verify that mediaRepository.save() was called with the expected paths and archived statuses
        verify(mediaRepository)
                .save(
                        argThat(
                                media ->
                                        media.getPath().equals(existingMedia1.getPath())
                                                && media.getArchivedStatus() == ArchivedStatus.OUT_OF_DATE));
        verify(mediaRepository)
                .save(
                        argThat(
                                media ->
                                        media.getPath().equals(existingMedia2.getPath())
                                                && media.getArchivedStatus() == ArchivedStatus.ARCHIVED));
    }

    @Test
    public void testScanLibraryTV() throws Exception {
        // Arrange
        Long libraryId = 1L;
        LibraryModel library = new LibraryModel();
        library.setName("Test Library");
        library.setCategory(MediaCategory.TV);
        library.setPath("testVolume/tv/");
        library.setId(libraryId);
        when(libraryRepository.findById(libraryId)).thenReturn(Optional.of(library));

        List<String> expectedMediaObjectPaths = new ArrayList<>();
        expectedMediaObjectPaths.add("testVolume/tv/Derry Girls/metadata");
        expectedMediaObjectPaths.add("testVolume/tv/Derry Girls/Season 01");
        expectedMediaObjectPaths.add("testVolume/tv/Derry Girls/Season 02");
        expectedMediaObjectPaths.add("testVolume/tv/Derry Girls/Season 03");
        expectedMediaObjectPaths.add("testVolume/tv/The Owl House/metadata");
        expectedMediaObjectPaths.add("testVolume/tv/The Owl House/Season 1");
        expectedMediaObjectPaths.add("testVolume/tv/The Owl House/Season 2");
        expectedMediaObjectPaths.add("testVolume/tv/The Owl House/Season 3");

        // Mock the behavior of mediaRepository without hitting the database
        when(mediaRepository.save(any(MediaModel.class))).thenReturn(null); // Adjust this as needed

        // Act
        libraryUpdateConsumer.scanLibrary(1L);

        // Assert
        verify(mediaRepository, times(8)).save(any(MediaModel.class));

        // Verify that mediaRepository.save() was called with the expected paths
        for (String expectedPath : expectedMediaObjectPaths) {
            verify(mediaRepository).save(argThat(media -> media.getPath().equals(expectedPath)));
        }
    }

    @Test
    public void testScanLibraryTVWithExistingMedia() throws Exception {
        // Arrange
        Long libraryId = 1L;
        LibraryModel library = new LibraryModel();
        library.setName("Test Library");
        library.setCategory(MediaCategory.TV);
        library.setPath("testVolume/tv/");
        library.setId(libraryId);
        when(libraryRepository.findById(libraryId)).thenReturn(Optional.of(library));

        // Mock the behavior of mediaRepository without hitting the database
        when(mediaRepository.save(any(MediaModel.class))).thenReturn(null); // Adjust this as needed

        // Mock existing media objects
        MediaModel existingMedia1 = new MediaModel();
        existingMedia1.setId(1L);
        existingMedia1.setPath("testVolume/tv/Derry Girls/metadata");
        existingMedia1.setArchivedStatus(ArchivedStatus.ARCHIVED);
        existingMedia1.setDateArchived(Instant.now());
        when(mediaRepository.findByPath(eq(existingMedia1.getPath()))).thenReturn(existingMedia1);

        MediaModel existingMedia2 = new MediaModel();
        existingMedia2.setId(2L);
        existingMedia2.setPath("testVolume/tv/Derry Girls/Season 01");
        existingMedia2.setArchivedStatus(ArchivedStatus.ARCHIVED);
        existingMedia2.setDateArchived(Instant.now());
        when(mediaRepository.findByPath(eq(existingMedia2.getPath()))).thenReturn(existingMedia2);

        MediaModel existingMedia3 = new MediaModel();
        existingMedia3.setId(3L);
        existingMedia3.setPath("testVolume/tv/Derry Girls/Season 02");
        existingMedia3.setArchivedStatus(ArchivedStatus.ARCHIVED);
        existingMedia3.setDateArchived(Instant.MIN); // Use the earliest possible date
        when(mediaRepository.findByPath(eq(existingMedia3.getPath()))).thenReturn(existingMedia3);

        MediaModel existingMedia4 = new MediaModel();
        existingMedia4.setId(4L);
        existingMedia4.setPath("testVolume/tv/Derry Girls/Season 03");
        existingMedia4.setArchivedStatus(ArchivedStatus.NOT_ARCHIVED);
        when(mediaRepository.findByPath(eq(existingMedia4.getPath()))).thenReturn(existingMedia4);

        // Act
        libraryUpdateConsumer.scanLibrary(1L);

        // Assert
        verify(mediaRepository, times(8)).save(any(MediaModel.class));

        // Verify that mediaRepository.save() was called with the expected paths and archived statuses
        verify(mediaRepository)
                .save(
                        argThat(
                                media ->
                                        media.getPath().equals(existingMedia1.getPath())
                                                && media.getArchivedStatus() == ArchivedStatus.ARCHIVED));
        verify(mediaRepository)
                .save(
                        argThat(
                                media ->
                                        media.getPath().equals(existingMedia2.getPath())
                                                && media.getArchivedStatus() == ArchivedStatus.ARCHIVED));
        verify(mediaRepository)
                .save(
                        argThat(
                                media ->
                                        media.getPath().equals(existingMedia3.getPath())
                                                && media.getArchivedStatus() == ArchivedStatus.OUT_OF_DATE));
        verify(mediaRepository)
                .save(
                        argThat(
                                media ->
                                        media.getPath().equals(existingMedia4.getPath())
                                                && media.getArchivedStatus() == ArchivedStatus.NOT_ARCHIVED));
    }

    @Test
    void testSynchronizeMedia() throws Exception {
        // Arrange
        Long libraryId = 1L;
        LibraryModel library = new LibraryModel();
        library.setBucketName("TestBucket");
        when(libraryRepository.findById(libraryId)).thenReturn(Optional.of(library));

        // Mock S3 Response
        S3Object s3Object = S3Object.builder().key("testKey").lastModified(Instant.now()).build();
        ListObjectsV2Response s3Response =
                ListObjectsV2Response.builder().contents(s3Object).isTruncated(false).build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(s3Response);

        MediaModel media = new MediaModel();
        media.setDateLastModified(Instant.now());
        when(mediaRepository.findByPath(s3Object.key())).thenReturn(media);

        // Act
        libraryUpdateConsumer.synchronizeLibrary(1L);

        // Assert
        verify(libraryRepository, times(1)).findById(libraryId);
        verify(s3Client, times(1)).listObjectsV2(any(ListObjectsV2Request.class));
        verify(mediaRepository, times(1)).findByPath(s3Object.key());
        verify(mediaRepository, times(1)).save(any(MediaModel.class));
    }
}
