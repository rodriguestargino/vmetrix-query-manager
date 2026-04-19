package com.vmetrix.querymanager.api.mapper;

import com.vmetrix.querymanager.api.dto.response.ValidationErrorDto;
import com.vmetrix.querymanager.domain.model.ValidationError;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Translates domain {@link ValidationError} instances into API-layer
 * {@link ValidationErrorDto} instances.
 *
 * <p>Keeps the {@code domain/} layer free of framework annotations (see
 * {@code CLAUDE.md} — Strict Rules §4, §5).
 */
@Mapper(componentModel = "spring")
public interface ValidationErrorMapper {

    ValidationErrorDto toDto(ValidationError error);

    List<ValidationErrorDto> toDtoList(List<ValidationError> errors);
}
