package com.anexya.app.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.anexya.app.api.CreateTagReadRequest;
import com.anexya.app.api.TagReadResponse;
import com.anexya.app.api.TagSummaryResponse;
import com.anexya.app.api.UpdateTagReadRequest;
import com.anexya.app.api.mapper.TagReadMapper;
import com.anexya.app.api.mapper.TagReadRequestMapper;
import com.anexya.app.api.mapper.TagSummaryMapper;
import com.anexya.app.domain.TagRead;
import com.anexya.app.service.AggregationStrategy;
import com.anexya.app.service.TagReadService;
import com.anexya.app.domain.TagReadCreate;
import com.anexya.app.domain.TagReadUpdate;
import com.anexya.app.domain.TagSummary;
import com.anexya.app.web.TagReadNotFoundException;

/**
 * Pure controller unit test: calls the controller directly with real MapStruct mappers and mocked service layer.
 * No Spring context or MVC stack involved.
 */
@ExtendWith(MockitoExtension.class)
class TagReadControllerTest {
    @Mock
    private TagReadService tagReadService;

    @Mock
    private AggregationStrategy aggregationStrategy;

    private TagReadMapper tagReadMapper;
    private TagSummaryMapper tagSummaryMapper;
    private TagReadRequestMapper tagReadRequestMapper;
    private TagReadController controller;

    @BeforeEach
    void setUp() {
        tagReadMapper = Mappers.getMapper(TagReadMapper.class);
        tagSummaryMapper = Mappers.getMapper(TagSummaryMapper.class);
        tagReadRequestMapper = Mappers.getMapper(TagReadRequestMapper.class);
        controller = new TagReadController(tagReadService, aggregationStrategy, tagReadMapper, tagSummaryMapper, tagReadRequestMapper);
    }

    @Test
    void getShouldMapResponse() {
        UUID id = UUID.randomUUID();
        TagRead domain = TagRead.builder()
                                .id(id)
                                .siteName("s")
                                .epc("e")
                                .build();
        when(tagReadService.get(id)).thenReturn(domain);

        TagReadResponse result = controller.get(id);

        assertThat(result).isEqualTo(tagReadMapper.toResponse(domain));
        verify(tagReadService).get(id);
    }

    @Test
    void getShouldPropagateNotFound() {
        UUID id = UUID.randomUUID();
        when(tagReadService.get(id)).thenThrow(new TagReadNotFoundException(id));

        assertThatThrownBy(() -> controller.get(id)).isInstanceOf(TagReadNotFoundException.class);
    }

    @Test
    void searchShouldDelegateAndMap() {
        TagRead domain = TagRead.builder()
                                .id(UUID.randomUUID())
                                .build();
        when(tagReadService.findByFilters(Optional.of("epc"), Optional.of("loc"), Optional.of("site"))).thenReturn(List.of(domain));

        List<TagReadResponse> results = controller.search("epc", "loc", "site");

        assertThat(results).containsExactly(tagReadMapper.toResponse(domain));
        verify(tagReadService).findByFilters(Optional.of("epc"), Optional.of("loc"), Optional.of("site"));
    }

    @Test
    void createShouldReturnCreatedResponse() {
        TagRead domain = TagRead.builder()
                                .id(UUID.randomUUID())
                                .build();
        when(tagReadService.create(any(TagReadCreate.class))).thenReturn(domain);

        var request = CreateTagReadRequest.builder()
                                          .siteName("site")
                                          .epc("epc")
                                          .referenceCode("ref")
                                          .location("loc")
                                          .rssi(-40.0)
                                          .readAt(Instant.parse("2024-01-01T00:00:00Z"))
                                          .build();

        ResponseEntity<TagReadResponse> response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(tagReadMapper.toResponse(domain));
    }

    @Test
    void updateShouldReturnMappedResponse() {
        UUID id = UUID.randomUUID();
        TagRead domain = TagRead.builder()
                                .id(id)
                                .build();
        when(tagReadService.update(any(), any(TagReadUpdate.class))).thenReturn(domain);

        UpdateTagReadRequest request = UpdateTagReadRequest.builder()
                                                           .siteName("site")
                                                           .epc("epc")
                                                           .referenceCode("ref")
                                                           .location("loc")
                                                           .rssi(-30.0)
                                                           .readAt(Instant.parse("2024-01-02T00:00:00Z"))
                                                           .build();

        TagReadResponse result = controller.update(id, request);

        assertThat(result).isEqualTo(tagReadMapper.toResponse(domain));
    }

    @Test
    void summarizeShouldMapResponses() {
        TagSummary summary = TagSummary.builder()
                                       .epc("epc")
                                       .totalReadCount(1)
                                       .averageRssi(0.0)
                                       .peakRssi(0.0)
                                       .locationCount(1)
                                       .mostDetectedLocation("loc")
                                       .firstSeen(Instant.EPOCH)
                                       .lastSeen(Instant.EPOCH)
                                       .build();
        when(aggregationStrategy.summarizeByTag(any(Instant.class), any(Instant.class), any(), any())).thenReturn(List.of(summary));

        List<TagSummaryResponse> results = controller.summarizeByEpc(Instant.EPOCH, Instant.now(), null, null);

        assertThat(results).containsExactly(tagSummaryMapper.toResponse(summary));
    }

    @Test
    void deleteShouldReturnNoContent() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> response = controller.delete(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(tagReadService).delete(id);
    }
}
