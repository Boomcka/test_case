package org.job.services.storage;

import org.job.services.utils.JSONUtils;
import org.job.services.utils.ZipUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.WeakHashMap;

@Service
public class FileSystemZipService implements ZipService {

    private final Path rootLocation;
    private final WeakHashMap<String, String> fileHashSum2FileName;
    private Path destinationCacheFile;
    private final String cacheFileName = "_cache.json";
    private final String zip = ".zip";


    @Autowired
    public FileSystemZipService(@NonNull ZipProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
        this.fileHashSum2FileName = new WeakHashMap<>();
    }

    @Override
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ZipException("Failed to zip and store empty file.");
        }
        if (cacheFileName.equals(file.getOriginalFilename())) {
            throw new ZipException(cacheFileName + "is system reserved name");
        }
    }

    @Override
    public String getMd5SumForFile(@NonNull MultipartFile file) {
        String md5;
        try (InputStream is = file.getInputStream()) {
            md5 = DigestUtils.md5DigestAsHex(is);
        } catch (IOException e) {
            throw new ZipException("Cannot calculate md5 sum for" + file.getOriginalFilename());
        }
        return md5;
    }

    @Override
    public boolean isContainedInCache(String md5SumForFile) {
        try {
            if (fileHashSum2FileName.containsKey(md5SumForFile) || isStoreInFileCache(md5SumForFile)) {
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new ZipException("Failed load cache from file" + e.getMessage());
        }
    }


    @Override
    public Resource getResourceFromCache(@NonNull String md5SumForFile) {
        if (fileHashSum2FileName.containsKey(md5SumForFile)) {
            return loadAsResource(fileHashSum2FileName.get(md5SumForFile));
        }
        throw new ZipException("Cache do not contained file with this md5 sum" + md5SumForFile);
    }

    @Override
    public Resource zip(@NonNull String md5SumForFile, @NonNull MultipartFile file) {
        Path location = getDestinationFile(file);
        try {
            File zipFile = location.toFile();
            removeFromCacheIfFileExist(zipFile);
            zipAndStore(file, location.toString(), md5SumForFile);
        } catch (IOException ex) {
            throw new ZipException("Failed to store file " + file.getOriginalFilename(), ex);
        }
        return loadAsResource(fileHashSum2FileName.get(md5SumForFile));
    }

    private void removeFromCacheIfFileExist(File zipFile) {
        if (zipFile.exists()) {
            if (!zipFile.delete()) {
                throw new IllegalStateException("Can not replace file");
            }
            fileHashSum2FileName.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(zipFile.toString()))
                    .map(e -> e.getKey())
                    .findFirst()
                    .ifPresent(key -> {
                        fileHashSum2FileName.remove(key);
                        try {
                            Map<String, String> cachedMap = JSONUtils.getCachedMap(destinationCacheFile.toString());
                            cachedMap.remove(key);
                            JSONUtils.writeCachedMap(destinationCacheFile.toString(), cachedMap);
                        } catch (IOException e) {
                            throw new ZipException("Cannot get cached store");
                        }
                    });
        }
    }


    private Path getDestinationFile(@NonNull MultipartFile file) {
        Path destinationFile = this.rootLocation.resolve(Paths.get(file.getOriginalFilename()))
                .normalize().toAbsolutePath();
        if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
            throw new ZipException("Cannot zip and store file outside current directory.");
        }
        destinationFile = Paths.get(destinationFile.toString() + zip);
        return destinationFile;
    }


    private void zipAndStore(@NonNull MultipartFile file, @NonNull String location, @NonNull String md5) throws IOException {
        ZipUtils.zip(file, location);
        fileHashSum2FileName.put(md5, location);
        JSONUtils.writeCachedMap(destinationCacheFile.toString(), md5, location);
    }


    private boolean isStoreInFileCache(@NonNull String md5) throws IOException {
        Path destinationCacheFile = this.rootLocation.resolve(
                Paths.get(cacheFileName)).normalize();
        Map<String, String> cachedMap = JSONUtils.getCachedMap(destinationCacheFile.toString());
        if (cachedMap.containsKey(md5)) {
            fileHashSum2FileName.put(md5, cachedMap.get(md5));
            return true;
        }
        return false;
    }

    private Path load(@NonNull String filename) {
        return rootLocation.resolve(filename);
    }

    private Resource loadAsResource(@NonNull String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new ZipException(
                        "Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new ZipException("Could not read file: " + filename, e);
        }
    }


    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new ZipException("Could not initialize storage", e);
        }
        fileHashSum2FileName.clear();
        destinationCacheFile = this.rootLocation.resolve(
                Paths.get(cacheFileName)).normalize();
        try (PrintWriter writer = new PrintWriter(destinationCacheFile.toFile());) {
            writer.print("");
        } catch (IOException e) {
            throw new ZipException("Failed to create cache File.");
        }
    }
}