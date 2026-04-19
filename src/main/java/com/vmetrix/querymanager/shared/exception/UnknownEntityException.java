package com.vmetrix.querymanager.shared.exception;

import lombok.Getter;

@Getter
public class UnknownEntityException extends RuntimeException {
    private final String entityName;

    public UnknownEntityException(String entityName) {
        super(String.format("Unknown entity: '%s' is not defined in metadata", entityName));
        this.entityName = entityName;
    }
}
