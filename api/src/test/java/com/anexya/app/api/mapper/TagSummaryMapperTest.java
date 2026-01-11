package com.anexya.app.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import com.anexya.app.api.TagSummaryResponse;
import com.anexya.app.service.model.TagSummary;

@ExtendWith(MockitoExtension.class)
class TagSummaryMapperTest {
    private final TagSummaryMapper mapper = Mappers.getMapper(TagSummaryMapper.class);

    @Test
    void toResponse_mapsAllFields() {
        Instant first = Instant.parse("2024-01-01T00:00:00Z");
        Instant last = Instant.parse("2024-01-02T00:00:00Z");
        TagSummary summary = TagSummary.builder()
                                       .epc("epc123")
                                       .totalReadCount(5)
                                       .averageRssi(-40.5)
                                       .peakRssi(-30.0)
                                       .locationCount(2)
                                       .mostDetectedLocation("Dock1")
                                       .firstSeen(first)
                                       .lastSeen(last)
                                       .build();

        TagSummaryResponse dto = mapper.toResponse(summary);

        assertThat(dto.epc()).isEqualTo("epc123");
        assertThat(dto.totalReadCount()).isEqualTo(5);
        assertThat(dto.averageRssi()).isEqualTo(-40.5);
        assertThat(dto.peakRssi()).isEqualTo(-30.0);
        assertThat(dto.locationCount()).isEqualTo(2);
        assertThat(dto.mostDetectedLocation()).isEqualTo("Dock1");
        assertThat(dto.firstSeen()).isEqualTo(first.toString());
        assertThat(dto.lastSeen()).isEqualTo(last.toString());
    }

    @Test
    void toResponse_handlesNulls() {
        TagSummary summary = TagSummary.builder()
                                       .epc("epc123")
                                       .totalReadCount(1)
                                       .averageRssi(0.0)
                                       .peakRssi(0.0)
                                       .locationCount(1)
                                       .mostDetectedLocation(null)
                                       .firstSeen(null)
                                       .lastSeen(null)
                                       .build();

        TagSummaryResponse dto = mapper.toResponse(summary);

        assertThat(dto.mostDetectedLocation()).isNull();
        assertThat(dto.firstSeen()).isNull();
        assertThat(dto.lastSeen()).isNull();
    }

    @Test
    void toResponse_usesAccessors() {
        TagSummary summary = mock(TagSummary.class);
        Instant first = Instant.parse("2024-02-01T00:00:00Z");
        Instant last = Instant.parse("2024-02-02T00:00:00Z");

        when(summary.epc()).thenReturn("epcM");
        when(summary.totalReadCount()).thenReturn(7L);
        when(summary.averageRssi()).thenReturn(-33.3);
        when(summary.peakRssi()).thenReturn(-20.0);
        when(summary.locationCount()).thenReturn(3L);
        when(summary.mostDetectedLocation()).thenReturn("DockM");
        when(summary.firstSeen()).thenReturn(first);
        when(summary.lastSeen()).thenReturn(last);

        TagSummaryResponse dto = mapper.toResponse(summary);

        assertThat(dto.epc()).isEqualTo("epcM");
        assertThat(dto.totalReadCount()).isEqualTo(7);
        assertThat(dto.averageRssi()).isEqualTo(-33.3);
        assertThat(dto.peakRssi()).isEqualTo(-20.0);
        assertThat(dto.locationCount()).isEqualTo(3);
        assertThat(dto.mostDetectedLocation()).isEqualTo("DockM");
        assertThat(dto.firstSeen()).isEqualTo(first.toString());
        assertThat(dto.lastSeen()).isEqualTo(last.toString());

        verify(summary, atLeastOnce()).epc();
        verify(summary, atLeastOnce()).totalReadCount();
        verify(summary, atLeastOnce()).averageRssi();
        verify(summary, atLeastOnce()).peakRssi();
        verify(summary, atLeastOnce()).locationCount();
        verify(summary, atLeastOnce()).mostDetectedLocation();
        verify(summary, atLeastOnce()).firstSeen();
        verify(summary, atLeastOnce()).lastSeen();
    }
}
