package org.job.services.utils;

import org.junit.function.ThrowingRunnable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

public class JSONUtilsTest {

    private static final Path CACHE_FILE_PATH = Paths.get("src", "test", "resources", "_cache.json");


    @BeforeEach
    public void clearCacheFile() throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(CACHE_FILE_PATH.toFile());) {
            writer.print("");
        }
    }

    @Test
    public void shouldWriteTwoEntitiesToFileWhenWriteCachedMapMethodCall() throws IOException {
        String md5First = "md5-1";
        JSONUtils.writeCachedMap(CACHE_FILE_PATH.toString(), md5First, "test_location");
        String md5Second = "md5-2";
        JSONUtils.writeCachedMap(CACHE_FILE_PATH.toString(), md5Second, "test_location");
        Map<String, String> cachedMap = JSONUtils.getCachedMap(CACHE_FILE_PATH.toString());
        assertThat(cachedMap).containsKeys(md5First, md5Second);
        assertThat(FileSystemUtils.deleteRecursively(CACHE_FILE_PATH.toFile())).isTrue();
    }

    @Test
    public void shouldThrowIllegalStateExceptionWhenStoreDuplicateHashSum() throws IOException {
        String md5First = "md5-1";
        JSONUtils.writeCachedMap(CACHE_FILE_PATH.toString(), md5First, "test_location");
        ThrowingRunnable throwingRunnable = () -> JSONUtils.writeCachedMap(CACHE_FILE_PATH.toString(), md5First, "test_location");
        final IllegalStateException exception = assertThrows(IllegalStateException.class, throwingRunnable);
        assertThat(exception).hasMessageStartingWith("There are duplicate md5 sum");
        assertThat(FileSystemUtils.deleteRecursively(CACHE_FILE_PATH.toFile())).isTrue();
    }

}
