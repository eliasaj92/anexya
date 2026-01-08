package com.anexya.app.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.anexya.app.api.TagSummaryResponse;
import com.anexya.app.service.TagSummary;

@ExtendWith(MockitoExtension.class)
class TagSummaryMapperTest
{

    private final TagSummaryMapper mapper = new TagSummaryMapper();

    @Test
    void toResponse_mapsAllFields()
    {
        Instant first = Instant.parse("2024-01-01T00:00:00Z");
        Instant last = Instant.parse("2024-01-02T00:00:00Z");
        TagSummary summary = new TagSummary("epc123", 5, -40.5, -30.0, 2, "Dock1", first, last);

        TagSummaryResponse dto = mapper.toResponse(summary);

        assertThat(dto.getEpc()).isEqualTo("epc123");
        assertThat(dto.getTotalReadCount()).isEqualTo(5);
        assertThat(dto.getAverageRssi()).isEqualTo(-40.5);
        assertThat(dto.getPeakRssi()).isEqualTo(-30.0);
        assertThat(dto.getLocationCount()).isEqualTo(2);
        assertThat(dto.getMostDetectedLocation()).isEqualTo("Dock1");
        assertThat(dto.getFirstSeen()).isEqualTo(first.toString());
        assertThat(dto.getLastSeen()).isEqualTo(last.toString());
    }

    @Test
    void toResponse_handlesNulls()
    {
        TagSummary summary = new TagSummary("epc123", 1, 0.0, 0.0, 1, null, null, null);

        TagSummaryResponse dto = mapper.toResponse(summary);

        assertThat(dto.getMostDetectedLocation()).isNull();
        assertThat(dto.getFirstSeen()).isNull();
        assertThat(dto.getLastSeen()).isNull();
    }

    @Test
    void toResponse_usesAccessors()
    {
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

        assertThat(dto.getEpc()).isEqualTo("epcM");
        assertThat(dto.getTotalReadCount()).isEqualTo(7);
        assertThat(dto.getAverageRssi()).isEqualTo(-33.3);
        assertThat(dto.getPeakRssi()).isEqualTo(-20.0);
        assertThat(dto.getLocationCount()).isEqualTo(3);
        assertThat(dto.getMostDetectedLocation()).isEqualTo("DockM");
        assertThat(dto.getFirstSeen()).isEqualTo(first.toString());
        assertThat(dto.getLastSeen()).isEqualTo(last.toString());

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
