package com.vmetrix.querymanager.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.sql.init.continue-on-error=true")
class OpenApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void should_expose_openapi_spec_with_all_four_endpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.paths['/api/query/build']").exists())
                .andExpect(jsonPath("$.paths['/api/query/validate']").exists())
                .andExpect(jsonPath("$.paths['/api/metadata/entities']").exists())
                .andExpect(jsonPath("$.paths['/api/metadata/comparators']").exists());
    }

    @Test
    void should_expose_swagger_ui_html() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void should_declare_api_metadata_in_openapi_info() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title", notNullValue()))
                .andExpect(jsonPath("$.info.title", containsString("VMetrix")))
                .andExpect(jsonPath("$.info.version", notNullValue()))
                .andExpect(jsonPath("$.info.description", notNullValue()));
    }

    @Test
    void should_include_request_example_for_build_endpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.paths['/api/query/build'].post.requestBody.content['application/json'].examples")
                        .exists());
    }

    @Test
    void should_include_response_example_for_build_endpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.paths['/api/query/build'].post.responses.200.content['application/json'].examples")
                        .exists());
    }

    @Test
    void should_tag_query_endpoints_with_query_api_tag() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/query/build'].post.tags[0]").value("Query API"))
                .andExpect(jsonPath("$.paths['/api/query/validate'].post.tags[0]").value("Query API"));
    }

    @Test
    void should_reference_error_response_schema_on_bad_request() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.paths['/api/query/build'].post.responses.400")
                        .exists());
    }
}
