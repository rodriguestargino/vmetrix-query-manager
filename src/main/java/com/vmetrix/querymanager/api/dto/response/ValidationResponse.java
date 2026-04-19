package com.vmetrix.querymanager.api.dto.response;

import com.vmetrix.querymanager.domain.model.ValidationError;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResponse {
    private boolean valid;
    @Builder.Default
    private List<ValidationError> errors = new java.util.ArrayList<>();
}
