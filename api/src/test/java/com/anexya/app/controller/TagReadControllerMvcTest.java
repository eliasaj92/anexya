package com.anexya.app.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.anexya.app.api.TagReadResponse;
import com.anexya.app.api.TagSummaryResponse;
import com.anexya.app.api.mapper.TagReadMapper;
import com.anexya.app.api.mapper.TagReadRequestMapper;
import com.anexya.app.api.mapper.TagSummaryMapper;
import com.anexya.app.domain.TagRead;
import com.anexya.app.service.AggregationStrategy;
import com.anexya.app.service.TagReadService;
import com.anexya.app.domain.TagReadCreate;
import com.anexya.app.domain.TagReadUpdate;
import com.anexya.app.domain.TagSummary;
import com.anexya.app.web.RequestLoggingFilter;
import com.anexya.app.web.TagReadNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Spring MVC slice test: spins up the web layer with MockMvc and mocks the service/mappers.
 * Verifies request/response wiring, validation, and status codes without starting the full application.
 */
@WebMvcTest(controllers = TagReadController.class, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RequestLoggingFilter.class)})
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("null")
class TagReadControllerMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TagReadService tagReadService;

    @MockBean
    private AggregationStrategy aggregationStrategy;

    @MockBean
    private TagReadMapper tagReadMapper;

    @MockBean
    private TagSummaryMapper tagSummaryMapper;

    @MockBean
    private TagReadRequestMapper tagReadRequestMapper;

    @Test
    void createAndGet_shouldReturnCreatedAndFetched() throws Exception {

        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        UUID id = UUID.randomUUID();
        TagRead created = TagRead.builder()
                                 .id(id)
                                 .siteName("Plant A")
                                 .epc("EPC123")
                                 .referenceCode("REF1")
                                 .location("Dock 1")
                                 .rssi(-45.5)
                                 .readAt(now)
                                 .build();

        TagReadResponse dto = TagReadResponse.builder()
                                             .id(id)
                                             .siteName("Plant A")
                                             .epc("EPC123")
                                             .referenceCode("REF1")
                                             .location("Dock 1")
                                             .rssi(-45.5)
                                             .readAt(now)
                                             .build();

        when(tagReadService.create(any(TagReadCreate.class))).thenReturn(created);
        when(tagReadService.get(id)).thenReturn(created);
        when(tagReadMapper.toResponse(created)).thenReturn(dto);
        when(tagReadRequestMapper.toCreate(any())).thenReturn(TagReadCreate.builder()
                                                                           .siteName("Plant A")
                                                                           .epc("EPC123")
                                                                           .referenceCode("REF1")
                                                                           .location("Dock 1")
                                                                           .rssi(-45.5)
                                                                           .readAt(now)
                                                                           .build());

        String payload = objectMapper.writeValueAsString(created.toBuilder()
                                                                .id(null)
                                                                .build());

        mockMvc.perform(post("/api/tag-reads").contentType(MediaType.APPLICATION_JSON)
                                              .content(payload))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.epc", equalTo("EPC123")))
               .andExpect(jsonPath("$.siteName", equalTo("Plant A")));

        mockMvc.perform(get("/api/tag-reads/" + id))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", equalTo(id.toString())));

        verify(tagReadService, times(1)).create(any(TagReadCreate.class));
        verify(tagReadService, times(1)).get(id);
    }

    @Test
    void create_shouldFailValidationOnMissingFields() throws Exception {
        String invalidPayload = "{\"epc\":\"EPC123\",\"referenceCode\":\"REF\",\"location\":\"Dock\",\"rssi\":-42.0}";

        mockMvc.perform(post("/api/tag-reads").contentType(MediaType.APPLICATION_JSON)
                                              .content(invalidPayload))
               .andExpect(status().isBadRequest());
    }

    @Test
    void search_shouldReturnMappedList() throws Exception {
        UUID id = UUID.randomUUID();
        TagRead found = TagRead.builder()
                               .id(id)
                               .siteName("S")
                               .epc("EPC-FILTER")
                               .referenceCode("REF")
                               .location("Dock")
                               .rssi(-40.0)
                               .readAt(Instant.EPOCH)
                               .build();
        TagReadResponse dto = TagReadResponse.builder()
                                             .id(id)
                                             .epc("EPC-FILTER")
                                             .siteName("S")
                                             .referenceCode("REF")
                                             .location("Dock")
                                             .rssi(-40.0)
                                             .readAt(Instant.EPOCH)
                                             .build();
        when(tagReadService.findByFilters(Optional.of("EPC-FILTER"), Optional.of("Dock"), Optional.empty())).thenReturn(List.of(found));
        when(tagReadMapper.toResponse(found)).thenReturn(dto);

        mockMvc.perform(get("/api/tag-reads/search").param("epc", "EPC-FILTER")
                                                    .param("location", "Dock"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].epc", equalTo("EPC-FILTER")));

        verify(tagReadService).findByFilters(Optional.of("EPC-FILTER"), Optional.of("Dock"), Optional.empty());
    }

    @Test
    void update_shouldReturnUpdated() throws Exception {
        Instant now = Instant.parse("2024-02-01T00:00:00Z");
        UUID id = UUID.randomUUID();
        TagRead updated = TagRead.builder()
                                 .id(id)
                                 .siteName("Plant B")
                                 .epc("EPC-SEARCH")
                                 .referenceCode("REF-SEARCH")
                                 .location("Lane 2")
                                 .rssi(-35.0)
                                 .readAt(now)
                                 .build();
        TagReadResponse dto = TagReadResponse.builder()
                                             .id(id)
                                             .siteName("Plant B")
                                             .epc("EPC-SEARCH")
                                             .referenceCode("REF-SEARCH")
                                             .location("Lane 2")
                                             .rssi(-35.0)
                                             .readAt(now)
                                             .build();

        when(tagReadService.update(ArgumentMatchers.eq(id), any(TagReadUpdate.class))).thenReturn(updated);
        when(tagReadMapper.toResponse(updated)).thenReturn(dto);
        when(tagReadRequestMapper.toUpdate(any())).thenReturn(TagReadUpdate.builder()
                                                                           .siteName("Plant B")
                                                                           .epc("EPC-SEARCH")
                                                                           .referenceCode("REF-SEARCH")
                                                                           .location("Lane 2")
                                                                           .rssi(-35.0)
                                                                           .readAt(now)
                                                                           .build());

        String updatePayload = objectMapper.writeValueAsString(updated.toBuilder()
                                                                      .id(null)
                                                                      .build());

        mockMvc.perform(put("/api/tag-reads/" + id).contentType(MediaType.APPLICATION_JSON)
                                                   .content(updatePayload))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.location", equalTo("Lane 2")))
               .andExpect(jsonPath("$.rssi", equalTo(-35.0)));
    }

    @Test
    void delete_shouldReturnNotFound() throws Exception {
        UUID missing = UUID.randomUUID();
        doThrow(new TagReadNotFoundException(missing)).when(tagReadService)
                                                      .delete(missing);

        mockMvc.perform(delete("/api/tag-reads/" + missing))
               .andExpect(status().isNotFound());
    }

    @Test
    void summary_shouldReturnAggregatedDtos() throws Exception {
        Instant start = Instant.parse("2024-03-01T00:00:00Z");
        Instant end = Instant.parse("2024-03-02T00:00:00Z");
        TagSummary summary = TagSummary.builder()
                                       .epc("EPC")
                                       .totalReadCount(2)
                                       .averageRssi(-30.0)
                                       .peakRssi(-25.0)
                                       .locationCount(1)
                                       .mostDetectedLocation("Dock")
                                       .firstSeen(start)
                                       .lastSeen(end)
                                       .build();
        TagSummaryResponse dto = TagSummaryResponse.builder()
                                                   .epc("EPC")
                                                   .totalReadCount(2)
                                                   .averageRssi(-30.0)
                                                   .peakRssi(-25.0)
                                                   .locationCount(1)
                                                   .mostDetectedLocation("Dock")
                                                   .firstSeen(start.toString())
                                                   .lastSeen(end.toString())
                                                   .build();

        when(aggregationStrategy.summarizeByTag(start, end, Optional.empty(), Optional.empty())).thenReturn(List.of(summary));
        when(tagSummaryMapper.toResponse(summary)).thenReturn(dto);

        mockMvc.perform(get("/api/tag-reads/summary/by-epc").param("startDate", start.toString())
                                                            .param("endDate", end.toString()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].epc", equalTo("EPC")))
               .andExpect(jsonPath("$[0].totalReadCount", equalTo(2)));
    }

    @Test
    void summary_shouldFailWhenMissingRequiredParams() throws Exception {
        Instant start = Instant.parse("2024-03-01T00:00:00Z");

        mockMvc.perform(get("/api/tag-reads/summary/by-epc").param("startDate", start.toString()))
               .andExpect(status().isBadRequest());
    }

    @Test
    void get_shouldReturnBadRequestForInvalidUuid() throws Exception {
        mockMvc.perform(get("/api/tag-reads/not-a-uuid"))
               .andExpect(status().isBadRequest());
    }

    @Test
    void search_shouldHandleEmptyFilters() throws Exception {
        when(tagReadService.findByFilters(Optional.empty(), Optional.empty(), Optional.empty())).thenReturn(List.of());

        mockMvc.perform(get("/api/tag-reads/search"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(0)));

        verify(tagReadService).findByFilters(Optional.empty(), Optional.empty(), Optional.empty());
    }
}
