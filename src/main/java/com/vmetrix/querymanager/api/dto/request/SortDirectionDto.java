package com.vmetrix.querymanager.api.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum SortDirectionDto {
    ASC, DESC;

    @JsonCreator
    public static SortDirectionDto fromString(String key) {
        if (key == null) return null;
        return SortDirectionDto.valueOf(key.toUpperCase());
    }
}
