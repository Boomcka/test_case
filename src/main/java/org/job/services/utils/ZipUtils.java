package org.job.services.utils;

import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ZipUtils {
    private ZipUtils() {
    }

    public static void zip(@NonNull MultipartFile file,@NonNull String location) throws IOException {
        try (
                ZipOutputStream zos = new ZipOutputStream(
                        new FileOutputStream(location));
                ByteArrayInputStream fis = new ByteArrayInputStream(file.getBytes());
        ) {
            ZipEntry zipEntry = new ZipEntry(file.getOriginalFilename());
            zos.putNextEntry(zipEntry);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
        }
    }
}
