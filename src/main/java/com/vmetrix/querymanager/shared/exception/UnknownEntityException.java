package com.vmetrix.querymanager.shared.exception;

public class UnknownEntityException extends RuntimeException {

    public UnknownEntityException(String logicalName) {
        super(String.format("Unknown entity: '%s' is not defined in metadata", logicalName));
    }
}
