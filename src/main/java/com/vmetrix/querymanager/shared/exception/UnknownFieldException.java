package com.vmetrix.querymanager.shared.exception;

import lombok.Getter;

@Getter
public class UnknownFieldException extends RuntimeException {
    private final String entityName;
    private final String fieldName;

    public UnknownFieldException(String entityName, String fieldName) {
        super(String.format("Unknown field: '%s' is not defined for entity '%s' in metadata", fieldName, entityName));
        this.entityName = entityName;
        this.fieldName = fieldName;
    }
}
