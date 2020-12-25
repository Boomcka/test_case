package org.job.services;

import org.job.services.storage.ZipException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ZipExceptionHandler extends ResponseEntityExceptionHandler {

    public ZipExceptionHandler() {
        super();
    }

    @ExceptionHandler(ZipException.class)
    public ResponseEntity<Map<String,String>> handleStorageFileNotFound(ZipException ex, RedirectAttributes redirectAttributes) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errors);
    }

    @ExceptionHandler({
            MaxUploadSizeExceededException.class,
    })
    public ResponseEntity<Map<String,String>> handleBadRequest(Exception ex, RedirectAttributes redirectAttributes) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", "File is too big");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}