package com.vmetrix.querymanager.shared.exception;

public class UnknownFieldException extends RuntimeException {

    public UnknownFieldException(String logicalEntityName, String logicalFieldName) {
        super(String.format("Unknown field: '%s' is not defined for entity '%s' in metadata", logicalFieldName, logicalEntityName));
    }
}
