package com.vmetrix.querymanager.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MetadataControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void should_return_all_entities_with_correct_counts_and_relationships() throws Exception {
        mockMvc.perform(get("/api/metadata/entities")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                // 1. Integration test: returns 200 with 3 entities
                .andExpect(jsonPath("$", hasSize(3)))
                // 2. Integration test: correct field count per entity (e.g. transaction has 13 fields)
                .andExpect(jsonPath("$[*].entity", containsInAnyOrder("transaction", "instrument", "party")))
                .andExpect(jsonPath("$[?(@.entity == 'transaction')].fields.length()").value(13))
                .andExpect(jsonPath("$[?(@.entity == 'instrument')].fields.length()").value(11))
                .andExpect(jsonPath("$[?(@.entity == 'party')].fields.length()").value(8))
                // 4. Integration test: relations array for TRANSACTION contains 2 relationships
                .andExpect(jsonPath("$[?(@.entity == 'transaction')].relations.length()").value(2))
                .andExpect(jsonPath("$[?(@.entity == 'party')].relations.length()").value(0));
    }

    @Test
    void should_return_comparators_grouped_by_data_type() throws Exception {
        // 3. Integration test: GET /api/metadata/comparators returns 200 with all 4 data types
        mockMvc.perform(get("/api/metadata/comparators")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.string", hasItems("equals", "notEquals", "like", "in", "notIn", "isNull", "isNotNull")))
                .andExpect(jsonPath("$.number", hasItems("equals", "notEquals", "greaterThan", "lessThan", "greaterOrEqual", "lessOrEqual", "between", "in", "isNull", "isNotNull")))
                .andExpect(jsonPath("$.date", hasItems("equals", "notEquals", "greaterThan", "lessThan", "greaterOrEqual", "lessOrEqual", "between", "isNull", "isNotNull")))
                .andExpect(jsonPath("$.timestamp", hasItems("equals", "notEquals", "greaterThan", "lessThan", "greaterOrEqual", "lessOrEqual", "isNull", "isNotNull")));
    }
}
