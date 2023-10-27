package com.example.mediaarchival.utils;

import com.example.mediaarchival.enums.MediaCategory;
import com.example.mediaarchival.models.MediaModel;
import com.example.mediaarchival.repositories.MediaRepository;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility class for handling TAR archive operations such as creating and unpacking TAR files.
 */
public class TarUtils {
  private static final String tempDirectory = EnvUtils.getTempDirectory();
  private static final Logger errorLogger = LoggerFactory.getLogger("ERROR_LOGGER");

  /**
   * Creates a TAR archive from the media provided.
   *
   * @param media the media to be archived
   * @param mediaRepository the media repository used to update the archiving status
   * @return the created TAR file
   * @throws RuntimeException if an I/O error occurs
   */
  public static File createTarArchive(MediaModel media, MediaRepository mediaRepository) {
    File tempFile = null;
    try {
      mediaRepository.updateIsTarringById(media.getId(), true);
      boolean isTvSeries = media.getLibrary().getCategory() == MediaCategory.TV;

      String sourceString = isTvSeries ? media.getPath().replace("/metadata", "") : media.getPath();

      tempFile = File.createTempFile("temp", ".tar", new File(tempDirectory));

      try (FileOutputStream fos = new FileOutputStream(tempFile);
          BufferedOutputStream bos = new BufferedOutputStream(fos);
          TarArchiveOutputStream tarOutputStream = new TarArchiveOutputStream(bos)) {

        tarOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        tarOutputStream.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);

        File file = new File(sourceString);
        addFileToTar(tarOutputStream, file, "", isTvSeries);
      }

      return tempFile;
    } catch (IOException e) {
      errorLogger.error("Error creating tar " + e.getMessage());
      if (tempFile != null) {
        tempFile.delete();
      }
      throw new RuntimeException(
          "Error creating TAR archive", e);
    } finally {
      mediaRepository.updateIsTarringById(media.getId(), false);
    }
  }

  /**
   * Unpacks a TAR archive to the directory where the TAR file is located.
   *
   * @param tarFile the TAR file to unpack
   * @throws IOException if an I/O error occurs
   */
  public static void unpackTarArchive(File tarFile) throws IOException {
    File destDir = tarFile.getParentFile(); // Get the directory of the tar file

    try (FileInputStream fis = new FileInputStream(tarFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        TarArchiveInputStream tarInputStream = new TarArchiveInputStream(bis)) {
      TarArchiveEntry entry;
      while ((entry = tarInputStream.getNextTarEntry()) != null) {
        File outputFile = new File(destDir, entry.getName());
        if (entry.isDirectory()) {
          if (!outputFile.exists()) {
            if (!outputFile.mkdirs()) {
              throw new IllegalStateException(
                  String.format("Failed to create directory %s.", outputFile.getAbsolutePath()));
            }
          }
        } else {
          try (FileOutputStream out = new FileOutputStream(outputFile)) {
            IOUtils.copy(tarInputStream, out);
          }
        }
      }
    } finally {
      tarFile.delete();
    }
  }

  private static void addFileToTar(
      TarArchiveOutputStream tarOutputStream, File file, String parent, boolean excludeSeasons)
      throws IOException {
    String entryName = parent + file.getName();;
    TarArchiveEntry tarEntry = new TarArchiveEntry(file, entryName);

    tarOutputStream.putArchiveEntry(tarEntry);

    if (file.isFile()) {
      try (FileInputStream fis = new FileInputStream(file);
          BufferedInputStream bis = new BufferedInputStream(fis)) {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = bis.read(buffer)) != -1) {
          tarOutputStream.write(buffer, 0, bytesRead);
        }
        tarOutputStream.closeArchiveEntry(); // Close the entry here after writing the file content
      }
    } else if (file.isDirectory()) {
      tarOutputStream.closeArchiveEntry(); // Close the entry here for directories
      File[] children = file.listFiles();
      if (children != null) {
        for (File child : children) {
          boolean isSeason = child.getName().toLowerCase().contains("season");
          if (!(isSeason && excludeSeasons)) {
            addFileToTar(tarOutputStream, child, entryName + "/", excludeSeasons);
          }
        }
      }
    }
  }
}
