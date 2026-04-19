package com.vmetrix.querymanager.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.sql.init.continue-on-error=true")
class QueryValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void should_return_200_for_valid_request() throws Exception {
        String jsonPayload = """
                {
                  "select": [
                    {"entity": "transaction", "field": "txnDate"},
                    {"entity": "counterparty", "field": "partyName"}
                  ],
                  "filters": {
                    "operator": "AND",
                    "conditions": [
                      {"entity": "transaction", "field": "status", "comparator": "equals", "value": "SETTLED"}
                    ]
                  }
                }
                """;

        mockMvc.perform(post("/api/query/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid", is(true)))
                .andExpect(jsonPath("$.errors", empty()));
    }

    @Test
    void should_return_400_for_unknown_entity() throws Exception {
        String jsonPayload = """
                {
                  "select": [
                    {"entity": "unknown_entity", "field": "txnDate"}
                  ]
                }
                """;

        mockMvc.perform(post("/api/query/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valid", is(false)))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].entity", is("unknown_entity")))
                .andExpect(jsonPath("$.errors[0].message", containsString("Entity unknown_entity does not exist")));
    }

    @Test
    void should_return_400_for_invalid_comparator() throws Exception {
        String jsonPayload = """
                {
                  "select": [
                    {"entity": "transaction", "field": "txnDate"}
                  ],
                  "filters": {
                    "operator": "AND",
                    "conditions": [
                      {"entity": "transaction", "field": "status", "comparator": "greaterThan", "value": "SETTLED"}
                    ]
                  }
                }
                """;

        mockMvc.perform(post("/api/query/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valid", is(false)))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].entity", is("transaction")))
                .andExpect(jsonPath("$.errors[0].field", is("status")))
                .andExpect(jsonPath("$.errors[0].comparator", is("greaterThan")))
                .andExpect(jsonPath("$.errors[0].message", containsString("not valid for string fields")));
    }
}
