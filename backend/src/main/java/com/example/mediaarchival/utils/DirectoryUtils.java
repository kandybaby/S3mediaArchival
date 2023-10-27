package com.example.mediaarchival.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class providing directory related operations such as creating directories and calculating directory sizes.
 */
public class DirectoryUtils {
  private static final Logger errorLogger = LoggerFactory.getLogger("ERROR_LOGGER");

  /**
   * Creates all directories up to but not including the last segment of the path.
   * This is meant for downloads, the last element in the path is the file being
   * downloaded, so it does not need to be created.
   *
   * @param path The full path where directories need to be created.
   * @throws IOException if an I/O error occurs or the parent directory does not exist and cannot be created.
   */
  public static void createDirectoriesExceptLast(String path) throws IOException {
    Path actualPath = Paths.get(path);
    Path parentPath = actualPath.getParent();
    if (parentPath != null) {
      Files.createDirectories(parentPath);
    }
  }

  /**
   * Calculates the size of a directory on disk, with an option to exclude certain subdirectories.
   * If the directory represents a TV series and the {@code isTVSeries} parameter is set,
   * directories containing "season" in the name are not included in the size calculation.
   *
   * @param dir The directory to calculate the size of.
   * @param isTVSeries Flag indicating if the directory represents a TV series.
   * @return The size of the directory in bytes.
   */
  public static long getDirectorySize(File dir, boolean isTVSeries) {
    long size = 0;
    try{
      if (dir.isFile()) {
        return dir.length();
      } else {
        File[] subFiles = dir.listFiles();
        if (subFiles != null) {
          for (File file : subFiles) {
            if (file.isFile()) {
              size += file.length();
            } else {
              if (isTVSeries
                  && file.isDirectory()
                  && file.getName().toLowerCase().contains("season")) {
                continue;
              }
              size += getDirectorySize(file, isTVSeries);
            }
          }
        }
      }
  } catch (Exception e) {
      errorLogger.error("Error getting directory size for " + dir.getPath() + "  " + e.getMessage());
  }
    return size;
  }
}
