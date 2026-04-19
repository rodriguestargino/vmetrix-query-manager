package com.vmetrix.querymanager.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryMetadataDto {
    private int columnCount;
    private int filterCount;
    private String generatedAt;
}
