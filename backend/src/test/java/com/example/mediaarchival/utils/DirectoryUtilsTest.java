package com.example.mediaarchival.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public class DirectoryUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void DirectoryUtils_createDirectoriesExceptLast_ShouldCreateParentDirectories() throws IOException {
        String testPath = tempDir.resolve("new/dir/structure/test.txt").toString();
        DirectoryUtils.createDirectoriesExceptLast(testPath);
        assertTrue(Files.exists(tempDir.resolve("new/dir/structure")));
    }

    @Test
    void DirectoryUtils_getDirectorySize_ShouldCalculateCorrectSize() throws IOException {
        Path newDir = tempDir.resolve("series");
        Files.createDirectories(newDir);

        // Create a file with size 1KB
        Path file = newDir.resolve("episode.mp4");
        Files.write(file, new byte[1024]);

        assertEquals(1024, DirectoryUtils.getDirectorySize(newDir.toFile(), false));
    }

    @Test
    void DirectoryUtils_getDirectorySize_ShouldExcludeSeasonFoldersForTVSeries() throws IOException {
        Path seriesDir = tempDir.resolve("series");
        Files.createDirectories(seriesDir);

        Path seasonDir = seriesDir.resolve("Season 1");
        Files.createDirectories(seasonDir);

        // Create a file with size 1KB in the series directory
        Path fileInSeries = seriesDir.resolve("info.txt");
        Files.write(fileInSeries, new byte[1024]);

        // Create a file with size 1KB in the season directory
        Path fileInSeason = seasonDir.resolve("episode.mp4");
        Files.write(fileInSeason, new byte[1024]);

        assertEquals(1024, DirectoryUtils.getDirectorySize(seriesDir.toFile(), true));
    }
}
