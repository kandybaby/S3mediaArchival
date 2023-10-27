package com.example.mediaarchival.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.mediaarchival.enums.MediaCategory;
import com.example.mediaarchival.models.LibraryModel;
import com.example.mediaarchival.models.MediaModel;
import com.example.mediaarchival.repositories.MediaRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TarUtilsTest {

  private MockedStatic<EnvUtils> mockEnvUtils;

  private File tarFile;

  @TempDir
  static Path tempDir;

  @Mock
  MediaRepository mediaRepository;

  @BeforeAll
  public void setup() throws IOException {
    mockEnvUtils = Mockito.mockStatic(EnvUtils.class);
    mockEnvUtils.when(EnvUtils::getTempDirectory).thenReturn("./testVolume");
  }

  @AfterEach
  void tearDown() throws IOException {
    if (tarFile != null && tarFile.exists()) {
      tarFile.delete();
    }
  }

  @Test
  public void TarUtils_createTarArchive_ForFile_ShouldCreateTarFileAndContainExpectedContent() throws Exception {
    MediaModel media = new MediaModel();
    media.setPath(
            "./testVolume/music/Chapell Roan/The Rise and Fall of a Midwest Princess/femininomenom.txt");
    media.setId(1L);
    LibraryModel library = new LibraryModel();
    library.setCategory(MediaCategory.OTHER);
    media.setLibrary(library);

    this.tarFile = TarUtils.createTarArchive(media, mediaRepository);

    assertNotNull(tarFile);
    assertTrue(tarFile.exists());

    verify(mediaRepository, times(1)).updateIsTarringById(1L, true);
    verify(mediaRepository, times(1)).updateIsTarringById(1L, false);
    try (TarArchiveInputStream tarInput = new TarArchiveInputStream(new FileInputStream(tarFile))) {
      TarArchiveEntry entry;
      Set<String> tarContents = new HashSet<>();

      while ((entry = tarInput.getNextTarEntry()) != null) {
        tarContents.add(entry.getName());
      }
      assertTrue(tarContents.contains("femininomenom.txt"));
    } finally {
      // Cleanup the tar file and the temporary directory
      tarFile.delete();
    }
  }

  @Test
  public void TarUtils_createTarArchive_ForDirectory_ShouldCreateTarFileAndContainExpectedDirectoryAndFiles() throws Exception {
    MediaModel media = new MediaModel();
    media.setPath("./testVolume/music");
    media.setId(1L);
    LibraryModel library = new LibraryModel();
    library.setCategory(MediaCategory.OTHER);
    media.setLibrary(library);

    File tarFile = TarUtils.createTarArchive(media, mediaRepository);

    assertNotNull(tarFile);
    assertTrue(tarFile.exists());

    verify(mediaRepository, times(1)).updateIsTarringById(1L, true);
    verify(mediaRepository, times(1)).updateIsTarringById(1L, false);

    try (TarArchiveInputStream tarInput = new TarArchiveInputStream(new FileInputStream(tarFile))) {
      TarArchiveEntry entry;
      Set<String> tarContents = new HashSet<>();

      while ((entry = tarInput.getNextTarEntry()) != null) {
        tarContents.add(entry.getName());
      }

      // Confirm the directory and files are in the tar contents
      assertTrue(tarContents.contains("music/"));
      assertTrue(tarContents.contains("music/Carly Rae Jepsen/"));
      assertTrue(tarContents.contains("music/Carly Rae Jepsen/The Loveliest Time/"));
      assertTrue(tarContents.contains("music/Carly Rae Jepsen/The Loveliest Time/kamikaze.txt"));
      assertTrue(tarContents.contains("music/Chapell Roan/"));
      assertTrue(
              tarContents.contains("music/Chapell Roan/The Rise and Fall of a Midwest Princess/"));
      assertTrue(
              tarContents.contains(
                      "music/Chapell Roan/The Rise and Fall of a Midwest Princess/femininomenom.txt"));
    } finally {
      tarFile.delete();
    }
  }

  @Test
  public void TarUtils_unpackTarArchive_ShouldUnpackFilesAndDeleteArchive() throws IOException {
    //Create TAR File
    MediaModel media = new MediaModel();
    media.setPath(
            "./testVolume/music/Chapell Roan/The Rise and Fall of a Midwest Princess/femininomenom.txt");
    media.setId(1L);
    LibraryModel library = new LibraryModel();
    library.setCategory(MediaCategory.OTHER);
    media.setLibrary(library);

    File tarFile = TarUtils.createTarArchive(media, mediaRepository);

    // Unpack the TAR file
    TarUtils.unpackTarArchive(tarFile);

    // Test if the expected file(s) after extraction exists
    File expectedFile = new File("./testVolume/femininomenom.txt");
    assertTrue(expectedFile.exists(), "Expected file should exist after unpacking the TAR archive");

    // Additionally, check that the tarFile no longer exists after unpacking
    assertFalse(tarFile.exists(), "Tar file should be deleted after unpacking");
    expectedFile.delete();
  }
}
