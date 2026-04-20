package com.vmetrix.querymanager.api.documentation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.sql.init.continue-on-error=true")
class OpenApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void should_pick_up_query_controller_documentation_from_interface() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                // Verify summary of /build
                .andExpect(jsonPath("$.paths['/api/query/build'].post.summary")
                        .value("Generate parameterized SQL from a query specification"))
                // Verify description of /build
                .andExpect(jsonPath("$.paths['/api/query/build'].post.description")
                        .value(containsString("shortest JOIN path using BFS")))
                // Verify summary of /execute
                .andExpect(jsonPath("$.paths['/api/query/execute'].post.summary")
                        .value("Execute a query and return actual data"))
                // Verify summary of /validate
                .andExpect(jsonPath("$.paths['/api/query/validate'].post.summary")
                        .value("Validate a query specification without generating SQL"));
    }

    @Test
    void should_pick_up_metadata_controller_documentation_from_interface() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                // Verify summary of /entities
                .andExpect(jsonPath("$.paths['/api/metadata/entities'].get.summary")
                        .value("Get all queryable entities"))
                // Verify summary of /comparators
                .andExpect(jsonPath("$.paths['/api/metadata/comparators'].get.summary")
                        .value("Get valid comparators"));
    }

    @Test
    void should_use_centralized_examples_in_build_endpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/query/build'].post.requestBody.content['application/json'].examples.specSection5-1.value.select[0].entity")
                        .value("transaction"))
                .andExpect(jsonPath("$.paths['/api/query/build'].post.responses.200.content['application/json'].examples.specSection5-1Response.value.sql")
                        .value(containsString("SELECT t.TXN_DATE")));
    }
}
