package com.vmetrix.querymanager.shared.exception;

import lombok.Getter;

@Getter
public class InvalidComparatorException extends RuntimeException {
    private final String comparator;

    public InvalidComparatorException(String comparator) {
        super(String.format("Unknown comparator: '%s' is not a valid comparator", comparator));
        this.comparator = comparator;
    }
}
