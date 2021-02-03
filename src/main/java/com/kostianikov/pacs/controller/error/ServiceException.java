package com.kostianikov.pacs.controller.error;

public class ServiceException extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }
}
