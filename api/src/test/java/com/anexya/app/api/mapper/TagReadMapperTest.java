package com.anexya.app.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.anexya.app.api.TagReadResponse;
import com.anexya.app.domain.TagRead;

@ExtendWith(MockitoExtension.class)
class TagReadMapperTest
{

    private final TagReadMapper mapper = new TagReadMapper();

    @Test
    void toResponse_mapsAllFields()
    {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        TagRead domain = TagRead.builder().id(id).siteName("site").epc("epc").referenceCode("ref").location("loc")
                .rssi(-42.0).readAt(now).build();

        TagReadResponse dto = mapper.toResponse(domain);

        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getSiteName()).isEqualTo("site");
        assertThat(dto.getEpc()).isEqualTo("epc");
        assertThat(dto.getReferenceCode()).isEqualTo("ref");
        assertThat(dto.getLocation()).isEqualTo("loc");
        assertThat(dto.getRssi()).isEqualTo(-42.0);
        assertThat(dto.getReadAt()).isEqualTo(now);
    }

    @Test
    void toResponse_handlesNull()
    {
        assertThat(mapper.toResponse(null)).isNull();
    }

    @Test
    void toResponse_usesAccessors()
    {
        TagRead domain = mock(TagRead.class);
        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2024-01-01T00:00:00Z");

        when(domain.getId()).thenReturn(id);
        when(domain.getSiteName()).thenReturn("site");
        when(domain.getEpc()).thenReturn("epc");
        when(domain.getReferenceCode()).thenReturn("ref");
        when(domain.getLocation()).thenReturn("loc");
        when(domain.getRssi()).thenReturn(-50.0);
        when(domain.getReadAt()).thenReturn(now);

        TagReadResponse dto = mapper.toResponse(domain);

        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getSiteName()).isEqualTo("site");
        assertThat(dto.getEpc()).isEqualTo("epc");
        assertThat(dto.getReferenceCode()).isEqualTo("ref");
        assertThat(dto.getLocation()).isEqualTo("loc");
        assertThat(dto.getRssi()).isEqualTo(-50.0);
        assertThat(dto.getReadAt()).isEqualTo(now);

        verify(domain).getId();
        verify(domain).getSiteName();
        verify(domain).getEpc();
        verify(domain).getReferenceCode();
        verify(domain).getLocation();
        verify(domain).getRssi();
        verify(domain).getReadAt();
    }
}
