package com.vmetrix.querymanager.shared.exception;

public class QueryBuildException extends RuntimeException {
    public QueryBuildException(String message) {
        super(message);
    }
    
    public QueryBuildException(String message, Throwable cause) {
        super(message, cause);
    }
}
