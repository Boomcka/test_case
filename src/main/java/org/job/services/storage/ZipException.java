package org.job.services.storage;


public class ZipException extends RuntimeException {

    public ZipException(String message) {
        super(message);
    }

    public ZipException(String message, Throwable cause) {
        super(message, cause);
    }
}