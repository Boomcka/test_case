package org.job.services;

import org.job.services.storage.ZipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Controller
public class ZipController {

    private final ZipService storageService;
    private final String uploadForm = "uploadForm";
    private final String headerValue = "attachment; filename=\"";

    @Autowired
    public ZipController(ZipService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String listUploadedFiles() throws IOException {
        return uploadForm;
    }


    @PostMapping("/")
    public ResponseEntity<Resource> handleFileUpload(@RequestParam("file") MultipartFile file, HttpServletResponse response) {
        storageService.validateFile(file);
        String md5SumForFile = storageService.getMd5SumForFile(file);
        HttpStatus httpStatus;
        Resource ans;
        if (storageService.isContainedInCache(md5SumForFile)) {
            httpStatus = HttpStatus.SEE_OTHER;
            ans = storageService.getResourceFromCache(md5SumForFile);
        } else {
            httpStatus = HttpStatus.OK;
            ans = storageService.zip(md5SumForFile, file);
        }
        return ResponseEntity.status(httpStatus).header(HttpHeaders.CONTENT_DISPOSITION,
                headerValue + ans.getFilename() + "\"").header("Etag", md5SumForFile).body(ans);
    }

}