package com.anexya.app.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import com.anexya.app.api.TagReadResponse;
import com.anexya.app.domain.TagRead;

@ExtendWith(MockitoExtension.class)
class TagReadMapperTest {
    private final TagReadMapper mapper = Mappers.getMapper(TagReadMapper.class);

    @Test
    void toResponse_mapsAllFields() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        TagRead domain = TagRead.builder()
                                .id(id)
                                .siteName("site")
                                .epc("epc")
                                .referenceCode("ref")
                                .location("loc")
                                .rssi(-42.0)
                                .readAt(now)
                                .build();

        TagReadResponse dto = mapper.toResponse(domain);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.siteName()).isEqualTo("site");
        assertThat(dto.epc()).isEqualTo("epc");
        assertThat(dto.referenceCode()).isEqualTo("ref");
        assertThat(dto.location()).isEqualTo("loc");
        assertThat(dto.rssi()).isEqualTo(-42.0);
        assertThat(dto.readAt()).isEqualTo(now);
    }

    @Test
    void toResponse_handlesNull() {
        assertThat(mapper.toResponse(null)).isNull();
    }

    @Test
    void toResponse_usesAccessors() {
        TagRead domain = mock(TagRead.class);
        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2024-01-01T00:00:00Z");

        when(domain.id()).thenReturn(id);
        when(domain.siteName()).thenReturn("site");
        when(domain.epc()).thenReturn("epc");
        when(domain.referenceCode()).thenReturn("ref");
        when(domain.location()).thenReturn("loc");
        when(domain.rssi()).thenReturn(-50.0);
        when(domain.readAt()).thenReturn(now);

        TagReadResponse dto = mapper.toResponse(domain);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.siteName()).isEqualTo("site");
        assertThat(dto.epc()).isEqualTo("epc");
        assertThat(dto.referenceCode()).isEqualTo("ref");
        assertThat(dto.location()).isEqualTo("loc");
        assertThat(dto.rssi()).isEqualTo(-50.0);
        assertThat(dto.readAt()).isEqualTo(now);

        verify(domain).id();
        verify(domain).siteName();
        verify(domain).epc();
        verify(domain).referenceCode();
        verify(domain).location();
        verify(domain).rssi();
        verify(domain).readAt();
    }
}
