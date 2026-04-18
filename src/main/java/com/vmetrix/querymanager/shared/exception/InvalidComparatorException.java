package com.vmetrix.querymanager.shared.exception;

public class InvalidComparatorException extends RuntimeException {

    public InvalidComparatorException(String comparatorName) {
        super(String.format("Unknown comparator: '%s' is not a valid comparator", comparatorName));
    }
}
