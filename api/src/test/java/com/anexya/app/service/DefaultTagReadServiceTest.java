package com.anexya.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.anexya.app.cloud.CloudLogger;
import com.anexya.app.cloud.CloudServiceFactory;
import com.anexya.app.cloud.MetricsPublisher;
import com.anexya.app.domain.TagRead;
import com.anexya.app.repository.TagReadRepository;
import com.anexya.app.web.TagReadNotFoundException;

@ExtendWith(MockitoExtension.class)
class DefaultTagReadServiceTest
{

    @Mock
    private TagReadRepository repository;

    @Mock
    private CloudServiceFactory cloudFactory;

    @Mock
    private CloudLogger cloudLogger;

    @Mock
    private MetricsPublisher metricsPublisher;

    @InjectMocks
    private DefaultTagReadService service;

    @Test
    void get_shouldReturnEntity()
    {
        UUID id = UUID.randomUUID();
        TagRead read = TagRead.builder().id(id).epc("EPC").siteName("Site").build();
        when(repository.findById(id)).thenReturn(Optional.of(read));

        TagRead result = service.get(id);

        assertThat(result).isSameAs(read);
    }

    @Test
    void get_shouldThrowWhenMissing()
    {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(id)).isInstanceOf(TagReadNotFoundException.class);
    }

    @Test
    void create_shouldPersistAndEmitMetrics()
    {
        Instant now = Instant.now();
        when(cloudFactory.logger()).thenReturn(cloudLogger);
        when(cloudFactory.metrics()).thenReturn(metricsPublisher);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TagRead created = service.create("SiteA", "EPC1", "REF1", "Dock", -40.0, now);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getReadAt()).isEqualTo(now);

        ArgumentCaptor<TagRead> savedCaptor = ArgumentCaptor.forClass(TagRead.class);
        verify(repository).save(savedCaptor.capture());
        assertThat(savedCaptor.getValue().getEpc()).isEqualTo("EPC1");

        verify(cloudLogger).log(eq("tag_read_created"), any(Map.class));
        verify(metricsPublisher).increment(eq("tag_reads.created"), eq(1.0), any(Map.class));
    }

    @Test
    void create_shouldDefaultReadAtWhenNull() {
        when(cloudFactory.logger()).thenReturn(cloudLogger);
        when(cloudFactory.metrics()).thenReturn(metricsPublisher);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TagRead created = service.create("SiteA", "EPC1", "REF1", "Dock", -40.0, null);

        assertThat(created.getReadAt()).isNotNull();
    }

    @Test
    void update_shouldPersistChangesAndRetainReadAtWhenNull()
    {
        UUID id = UUID.randomUUID();
        Instant originalReadAt = Instant.parse("2024-01-01T00:00:00Z");
        TagRead existing = TagRead.builder().id(id).siteName("SiteA").epc("EPC1").referenceCode("REF1")
                .location("Dock1").rssi(-50.0).readAt(originalReadAt).build();

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cloudFactory.logger()).thenReturn(cloudLogger);
        when(cloudFactory.metrics()).thenReturn(metricsPublisher);

        TagRead updated = service.update(id, "SiteB", "EPC2", "REF2", "Dock2", -30.0, null);

        assertThat(updated.getSiteName()).isEqualTo("SiteB");
        assertThat(updated.getEpc()).isEqualTo("EPC2");
        assertThat(updated.getReadAt()).isEqualTo(originalReadAt); // retained when null

        verify(repository).save(any(TagRead.class));
        verify(cloudLogger).log(eq("tag_read_updated"), any(Map.class));
        verify(metricsPublisher).increment(eq("tag_reads.updated"), eq(1.0), any(Map.class));
    }

    @Test
    void update_shouldUseProvidedReadAtWhenNotNull()
    {
        UUID id = UUID.randomUUID();
        Instant supplied = Instant.parse("2024-02-02T00:00:00Z");
        TagRead existing = TagRead.builder().id(id).siteName("SiteA").epc("EPC1").referenceCode("REF1")
                .location("Dock1").rssi(-50.0).readAt(Instant.parse("2024-01-01T00:00:00Z")).build();

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cloudFactory.logger()).thenReturn(cloudLogger);
        when(cloudFactory.metrics()).thenReturn(metricsPublisher);

        TagRead updated = service.update(id, "SiteB", "EPC2", "REF2", "Dock2", -30.0, supplied);

        assertThat(updated.getReadAt()).isEqualTo(supplied);
    }

    @Test
    void delete_shouldThrowWhenNotFound()
    {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id)).isInstanceOf(TagReadNotFoundException.class);
    }

    @Test
    void delete_shouldRemoveAndEmitMetrics()
    {
        UUID id = UUID.randomUUID();
        TagRead existing = TagRead.builder().id(id).build();
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(cloudFactory.logger()).thenReturn(cloudLogger);
        when(cloudFactory.metrics()).thenReturn(metricsPublisher);

        service.delete(id);

        verify(repository).deleteById(id);
        verify(cloudLogger).log(eq("tag_read_deleted"), any(Map.class));
        verify(metricsPublisher).increment(eq("tag_reads.deleted"), eq(1.0), any(Map.class));
    }

    @Test
    void findByFilters_shouldDelegateToRepository() {
        when(repository.findByFilters(Optional.of("E"), Optional.empty(), Optional.of("S")))
                .thenReturn(List.of());

        List<TagRead> results = service.findByFilters(Optional.of("E"), Optional.empty(), Optional.of("S"));

        assertThat(results).isEmpty();
        verify(repository).findByFilters(Optional.of("E"), Optional.empty(), Optional.of("S"));
    }
}
