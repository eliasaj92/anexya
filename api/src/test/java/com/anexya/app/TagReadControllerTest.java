package com.anexya.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class TagReadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateListAndSummarize() throws Exception {
        String payload = String.format("{\"siteName\":\"Plant A\",\"epc\":\"EPC123\",\"referenceCode\":\"REF1\",\"location\":\"Dock 1\",\"rssi\":-45.5,\"readAt\":\"%s\"}",
                Instant.now().toString());

        var createResponse = mockMvc.perform(post("/api/tag-reads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode created = objectMapper.readTree(createResponse.getResponse().getContentAsString());
        assertThat(created.get("epc").asText()).isEqualTo("EPC123");

        var listResponse = mockMvc.perform(get("/api/tag-reads"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode list = objectMapper.readTree(listResponse.getResponse().getContentAsString());
        assertThat(list.size()).isGreaterThanOrEqualTo(1);

        var summaryResponse = mockMvc.perform(get("/api/tag-reads/summary/by-epc")
                        .param("startDate", Instant.now().minusSeconds(3600).toString())
                        .param("endDate", Instant.now().plusSeconds(3600).toString()))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode summary = objectMapper.readTree(summaryResponse.getResponse().getContentAsString());
        assertThat(summary.get(0).get("epc").asText()).isEqualTo("EPC123");
        assertThat(summary.get(0).get("totalReadCount").asLong()).isGreaterThanOrEqualTo(1);
    }
}
