package org.job.services.utils;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.springframework.lang.NonNull;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public final class JSONUtils {

    private JSONUtils() {

    }

    public static void writeCachedMap(@NonNull String location, @NonNull String md5,
                                      @NonNull String attributeFileLocation) throws IOException {
        Gson gson = new Gson();
        Map<String, String> stringMap = readCachedMap(location);
        if (stringMap.containsKey(md5)) {
            throw new IllegalStateException("There are duplicate md5 sum " + md5 + "in file cache");
        }
        stringMap.put(md5, attributeFileLocation);
        writeCachedMap(location, stringMap);
    }

    public static void writeCachedMap(@NonNull String cacheFileLocation, Map<String, String> map) throws IOException {
        Gson gson = new Gson();
        try (Writer writer = new FileWriter(cacheFileLocation)) {
            gson.toJson(map, writer);
        }
    }

    public static Map<String, String> getCachedMap(@NonNull String cacheFileLocation) throws IOException {
        return readCachedMap(cacheFileLocation);
    }

    private static Map<String, String> readCachedMap(@NonNull String cacheFileLocation) throws IOException {
        Gson gson = new Gson();
        try (JsonReader reader = new JsonReader(new FileReader(cacheFileLocation))) {
            Map<String, String> stringMap = gson.fromJson(reader, Map.class);
            if (stringMap == null) {
                stringMap = new HashMap<>();
            }
            return stringMap;

        }
    }
}