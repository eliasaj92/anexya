package com.anexya.app.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.anexya.app.api.CreateTagReadRequest;
import com.anexya.app.api.TagReadResponse;
import com.anexya.app.api.TagSummaryResponse;
import com.anexya.app.api.UpdateTagReadRequest;
import com.anexya.app.api.mapper.TagReadMapper;
import com.anexya.app.api.mapper.TagSummaryMapper;
import com.anexya.app.domain.TagRead;
import com.anexya.app.service.AggregationStrategy;
import com.anexya.app.service.TagReadService;
import com.anexya.app.service.TagSummary;
import com.anexya.app.web.TagReadNotFoundException;

@ExtendWith(MockitoExtension.class)
class TagReadControllerTest
{

    @Mock
    private TagReadService tagReadService;

    @Mock
    private AggregationStrategy aggregationStrategy;

    @Mock
    private TagReadMapper tagReadMapper;

    @Mock
    private TagSummaryMapper tagSummaryMapper;

    @InjectMocks
    private TagReadController controller;

    @Test
    void getShouldMapResponse()
    {
        UUID id = UUID.randomUUID();
        TagRead domain = TagRead.builder().id(id).siteName("s").epc("e").build();
        TagReadResponse dto = TagReadResponse.builder().id(id).build();
        when(tagReadService.get(id)).thenReturn(domain);
        when(tagReadMapper.toResponse(domain)).thenReturn(dto);

        TagReadResponse result = controller.get(id);

        assertThat(result).isSameAs(dto);
        verify(tagReadService).get(id);
        verify(tagReadMapper).toResponse(domain);
    }

    @Test
    void getShouldPropagateNotFound()
    {
        UUID id = UUID.randomUUID();
        when(tagReadService.get(id)).thenThrow(new TagReadNotFoundException(id));

        assertThatThrownBy(() -> controller.get(id)).isInstanceOf(TagReadNotFoundException.class);
    }

    @Test
    void searchShouldDelegateAndMap()
    {
        TagRead domain = TagRead.builder().id(UUID.randomUUID()).build();
        TagReadResponse dto = TagReadResponse.builder().id(domain.getId()).build();
        when(tagReadService.findByFilters(Optional.of("epc"), Optional.of("loc"), Optional.of("site")))
                .thenReturn(List.of(domain));
        when(tagReadMapper.toResponse(domain)).thenReturn(dto);

        List<TagReadResponse> results = controller.search("epc", "loc", "site");

        assertThat(results).containsExactly(dto);
        verify(tagReadService).findByFilters(Optional.of("epc"), Optional.of("loc"), Optional.of("site"));
        verify(tagReadMapper).toResponse(domain);
    }

    @Test
    void createShouldReturnCreatedResponse()
    {
        TagRead domain = TagRead.builder().id(UUID.randomUUID()).build();
        TagReadResponse dto = TagReadResponse.builder().id(domain.getId()).build();
        when(tagReadService.create(anyString(), anyString(), anyString(), anyString(), anyDouble(), any(Instant.class)))
                .thenReturn(domain);
        when(tagReadMapper.toResponse(domain)).thenReturn(dto);

        var request = new CreateTagReadRequest();
        request.setSiteName("site");
        request.setEpc("epc");
        request.setReferenceCode("ref");
        request.setLocation("loc");
        request.setRssi(-40.0);
        request.setReadAt(Instant.parse("2024-01-01T00:00:00Z"));

        ResponseEntity<TagReadResponse> response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(dto);
    }

    @Test
    void updateShouldReturnMappedResponse()
    {
        UUID id = UUID.randomUUID();
        TagRead domain = TagRead.builder().id(id).build();
        TagReadResponse dto = TagReadResponse.builder().id(id).build();
        when(tagReadService.update(any(), anyString(), anyString(), anyString(), anyString(), anyDouble(),
                any(Instant.class))).thenReturn(domain);
        when(tagReadMapper.toResponse(domain)).thenReturn(dto);

        UpdateTagReadRequest request = new UpdateTagReadRequest();
        request.setSiteName("site");
        request.setEpc("epc");
        request.setReferenceCode("ref");
        request.setLocation("loc");
        request.setRssi(-30.0);
        request.setReadAt(Instant.parse("2024-01-02T00:00:00Z"));

        TagReadResponse result = controller.update(id, request);

        assertThat(result).isSameAs(dto);
    }

    @Test
    void summarizeShouldMapResponses()
    {
        TagSummary summary = new TagSummary("epc", 1, 0.0, 0.0, 1, "loc", Instant.EPOCH, Instant.EPOCH);
        TagSummaryResponse dto = new TagSummaryResponse("epc", 1, 0.0, 0.0, 1, "loc", Instant.EPOCH.toString(),
                Instant.EPOCH.toString());
        when(aggregationStrategy.summarizeByTag(any(Instant.class), any(Instant.class), any(), any()))
                .thenReturn(List.of(summary));
        when(tagSummaryMapper.toResponse(summary)).thenReturn(dto);

        List<TagSummaryResponse> results = controller.summarizeByEpc(Instant.EPOCH, Instant.now(), null, null);

        assertThat(results).containsExactly(dto);
    }

    @Test
    void deleteShouldReturnNoContent()
    {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> response = controller.delete(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(tagReadService).delete(id);
    }
}
