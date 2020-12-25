package org.job.services.storage;

import org.job.services.utils.JSONUtils;
import org.junit.function.ThrowingRunnable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class FilesystemStorageServiceTest {
    private ZipProperties properties = new ZipProperties();
    private FileSystemZipService service;
    private MockMultipartFile multipartFile = new MockMultipartFile("foo", "foo.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "Hello, World".getBytes());


    @BeforeEach
    public void init() {
        properties.setLocation("target/files/" + Math.abs(new Random().nextLong()));
        service = new FileSystemZipService(properties);
        service.deleteAll();
        service.init();
    }

    @Test
    public void shouldThrowExceptionWhenInputFileIsNull() {
        ThrowingRunnable throwingRunnable = () -> service.validateFile(null);
        final ZipException exception = assertThrows(ZipException.class, throwingRunnable);
        assertThat(exception).hasMessageStartingWith("Failed to zip and store empty file");
    }

    @Test
    public void shouldThrowExceptionWhenInputFileIsEmpty() {
        MultipartFile mock = Mockito.mock(MultipartFile.class);
        Mockito.when(mock.isEmpty()).thenReturn(true);
        ThrowingRunnable throwingRunnable = () -> service.validateFile(mock);
        final ZipException exception = assertThrows(ZipException.class, throwingRunnable);
        assertThat(exception).hasMessageStartingWith("Failed to zip and store empty file");
    }

    @Test
    public void shouldThrowExceptionWhenInputFileNameEqualCacheFileName() {
        String cachedFileName = "_cache.json";
        MultipartFile mock = Mockito.mock(MultipartFile.class);
        Mockito.when(mock.isEmpty()).thenReturn(false);
        Mockito.when(mock.getOriginalFilename()).thenReturn(cachedFileName);
        ThrowingRunnable throwingRunnable = () -> service.validateFile(mock);
        ZipException exception = assertThrows(ZipException.class, throwingRunnable);
        assertThat(exception).hasMessageStartingWith(cachedFileName + "is system reserved name");
    }

    @Test
    public void shouldReturnMd5SumForFile() throws IOException {
        String md5;
        try (InputStream is = multipartFile.getInputStream()) {
            md5 = DigestUtils.md5DigestAsHex(is);
        }
        String md5SumForFile = service.getMd5SumForFile(multipartFile);
        assertThat(md5SumForFile).isEqualTo(md5);
    }

    @Test
    public void shouldReturnFalseWhenFileNotInCache() throws IOException {
        assertFalse(service.isContainedInCache("test"));
        service.deleteAll();
    }

    @Test
    public void shouldReturnTrueWhenFileInCache() throws IOException {
        service.zip("foo", multipartFile);
        assertTrue(service.isContainedInCache("foo"));
    }

    @Test
    public void shouldReturnTrueWhenFileInFileCache() throws IOException {
        Path path = Paths.get(properties.getLocation(), "_cache.json");
        try (PrintWriter writer = new PrintWriter(path.toFile());) {
            writer.print("");
        }
        JSONUtils.writeCachedMap(path.toString(), "foo", "foo.txt");
        assertTrue(service.isContainedInCache("foo"));
    }

    @Test
    public void shouldReturnZipResource() throws IOException {
        String md5SumForFile = service.getMd5SumForFile(multipartFile);
        service.zip(md5SumForFile, multipartFile);
        Resource resourceFromCache = service.getResourceFromCache(md5SumForFile);
        assertThat(resourceFromCache.exists()).isTrue();
    }

    @Test
    public void shouldThrowNewExceptionWhenCallGetResourceFromCacheWithWrongSum() throws IOException {
        ThrowingRunnable throwingRunnable = () -> service.getResourceFromCache("foo");
        ZipException exception = assertThrows(ZipException.class, throwingRunnable);
        assertThat(exception).hasMessageStartingWith("Cache do not contained file with this md5 sum");
    }

    @Test
    public void shouldRewriteFileWithSameName() throws IOException {
        MockMultipartFile multipartFile2 = new MockMultipartFile("foo", "foo.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "O, Hi Mark".getBytes());
        String md5SumForFile1 = service.getMd5SumForFile(multipartFile);
        String md5SumForFile2 = service.getMd5SumForFile(multipartFile2);
        service.zip(md5SumForFile1,multipartFile);
        service.zip(md5SumForFile2,multipartFile2);
        ThrowingRunnable throwingRunnable = () -> service.getResourceFromCache(md5SumForFile1);
        ZipException exception = assertThrows(ZipException.class, throwingRunnable);
        assertThat(exception).hasMessageStartingWith("Cache do not contained file with this md5 sum");
        assertThat(service.isContainedInCache(md5SumForFile2)).isTrue();
    }
}
