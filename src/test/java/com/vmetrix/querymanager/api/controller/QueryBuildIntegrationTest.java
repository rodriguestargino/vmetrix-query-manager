package com.vmetrix.querymanager.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.sql.init.continue-on-error=true")
class QueryBuildIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void should_build_query_golden_path() throws Exception {
        String jsonPayload = "{\n" +
                "  \"select\": [\n" +
                "    {\"entity\": \"transaction\", \"field\": \"txnDate\"},\n" +
                "    {\"entity\": \"counterparty\", \"field\": \"partyName\", \"alias\": \"counterpartyName\"}\n" +
                "  ],\n" +
                "  \"filters\": {\n" +
                "    \"operator\": \"AND\",\n" +
                "    \"conditions\": [\n" +
                "      {\"entity\": \"transaction\", \"field\": \"status\", \"comparator\": \"equals\", \"value\": \"SETTLED\"}\n" +
                "    ]\n" +
                "  },\n" +
                "  \"sorting\": [\n" +
                "    {\"entity\": \"transaction\", \"field\": \"txnDate\", \"direction\": \"desc\"}\n" +
                "  ],\n" +
                "  \"maxResults\": 500\n" +
                "}";

        mockMvc.perform(post("/api/query/build")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    void should_build_query_with_only_select() throws Exception {
        String jsonPayload = "{\n" +
                "  \"select\": [\n" +
                "    {\"entity\": \"transaction\", \"field\": \"txnDate\"}\n" +
                "  ]\n" +
                "}";

        mockMvc.perform(post("/api/query/build")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sql", containsString("FROM TRANSACTION t")))
                .andExpect(jsonPath("$.sql", not(containsString("WHERE"))))
                .andExpect(jsonPath("$.metadata.columnCount", is(1)))
                .andExpect(jsonPath("$.metadata.filterCount", is(0)));
    }

    @Test
    void should_return_400_for_unknown_entity_in_select() throws Exception {
        String jsonPayload = "{\n" +
                "  \"select\": [\n" +
                "    {\"entity\": \"unknown_entity\", \"field\": \"txnDate\"}\n" +
                "  ]\n" +
                "}";

        mockMvc.perform(post("/api/query/build")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("is not defined in metadata")));
    }
}
