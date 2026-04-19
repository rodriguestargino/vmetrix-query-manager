package com.vmetrix.querymanager.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A single column projection in the SELECT clause")
public class SelectFieldDto {

    @NotBlank(message = "Entity name must not be blank")
    @Schema(description = "Logical entity name or relation alias (camelCase). "
            + "Examples: 'transaction', 'instrument', 'counterparty', 'issuer'.",
            example = "transaction", required = true)
    private String entity;

    @NotBlank(message = "Field name must not be blank")
    @Schema(description = "Logical field name (camelCase). Resolved to the physical "
            + "column name via metadata lookup.",
            example = "txnDate", required = true)
    private String field;

    @Schema(description = "Optional SQL alias for the projected column. "
            + "When absent, no 'AS' clause is emitted.",
            example = "counterpartyName")
    private String alias;
}
