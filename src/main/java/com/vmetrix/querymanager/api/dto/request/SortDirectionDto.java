package com.vmetrix.querymanager.api.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ORDER BY direction. Accepted case-insensitively in requests.",
        allowableValues = {"ASC", "DESC"})
public enum SortDirectionDto {
    ASC, DESC;

    @JsonCreator
    public static SortDirectionDto fromString(String key) {
        if (key == null) return null;
        return SortDirectionDto.valueOf(key.toUpperCase());
    }
}
