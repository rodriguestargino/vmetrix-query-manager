package com.vmetrix.querymanager.domain.engine.filter;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

/**
 * Value object holding the result of a filter rendering operation:
 * a SQL fragment and its associated bind parameters.
 */
@Value
@Builder
public class FilterResult {

    /** SQL fragment (e.g. "t.STATUS = :p1" or "(t.STATUS = :p1 AND t.AMOUNT > :p2)") */
    String sqlFragment;

    /** Bind parameters (e.g. {"p1": "SETTLED", "p2": 1000000}) */
    Map<String, Object> parameters;
}
