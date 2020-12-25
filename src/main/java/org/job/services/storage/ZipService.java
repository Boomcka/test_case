package org.job.services.storage;


import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

public interface ZipService {

    void init();

    void deleteAll();

    void validateFile(MultipartFile file);

    String getMd5SumForFile(@NonNull MultipartFile file);

    boolean isContainedInCache(@NonNull String md5SumForFile);

    /**
     * It this project call this method if your sure that cache contained md5 (call before StorageService.isContainedInCache)
     *
     * @param md5SumForFile
     * @return
     */
    Resource getResourceFromCache(@NonNull String md5SumForFile);

    Resource zip(@NonNull String md5SumForFile, @NonNull MultipartFile file);
}