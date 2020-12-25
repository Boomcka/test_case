package org.job.services.utils;


import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ZipUtilsTest {

    @Mock
    MultipartFile multipartFile;
    private static File tempFile;


    @Test
    public void shouldZipFile() throws IOException {
        Path path = Paths.get("src", "test", "resources", "test.txt");
        try (PrintWriter writer = new PrintWriter(path.toFile());) {
            writer.print("Hello www.myoffice.ru");
        }
        when(multipartFile.getBytes()).thenReturn(FileUtils.readFileToByteArray(path.toFile()));
        when(multipartFile.getOriginalFilename()).thenReturn("test");
        Path zip = Paths.get("src", "test", "resources", "out.zip");
        ZipUtils.zip(multipartFile, zip.toString());
        assertThat(Files.exists(zip)).isTrue();
        assertThat(FileSystemUtils.deleteRecursively(path.toFile())).isTrue();
        assertThat(FileSystemUtils.deleteRecursively(zip.toFile())).isTrue();
    }
}
