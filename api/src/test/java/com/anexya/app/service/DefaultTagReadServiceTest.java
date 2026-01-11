package com.anexya.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import com.anexya.app.cloud.CloudLogger;
import com.anexya.app.cloud.CloudServiceFactory;
import com.anexya.app.cloud.MetricsPublisher;
import com.anexya.app.domain.TagRead;
import com.anexya.app.repository.TagReadRepository;
import com.anexya.app.service.impl.DefaultTagReadService;
import com.anexya.app.service.model.TagReadCreate;
import com.anexya.app.service.model.TagReadUpdate;
import com.anexya.app.web.TagReadNotFoundException;

@ExtendWith(MockitoExtension.class)
class DefaultTagReadServiceTest {
    @Mock
    private TagReadRepository repository;

    @Mock
    private ObjectProvider<CloudServiceFactory> cloudFactoryProvider;

    @Mock
    private CloudServiceFactory cloudFactory;

    @Mock
    private CloudLogger cloudLogger;

    @Mock
    private MetricsPublisher metricsPublisher;

    @InjectMocks
    private DefaultTagReadService service;

    @Test
    void get_shouldReturnEntity() {
        UUID id = UUID.randomUUID();
        TagRead read = TagRead.builder()
                              .id(id)
                              .epc("EPC")
                              .siteName("Site")
                              .build();
        when(repository.findById(id)).thenReturn(Optional.of(read));

        TagRead result = service.get(id);

        assertThat(result).isSameAs(read);
    }

    @Test
    void get_shouldThrowWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(id)).isInstanceOf(TagReadNotFoundException.class);
    }

    @Test
    void create_shouldPersistAndEmitMetrics() {
        Instant now = Instant.now();
        stubCloudAvailable();
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TagRead created = service.create(TagReadCreate.builder()
                                                      .siteName("SiteA")
                                                      .epc("EPC1")
                                                      .referenceCode("REF1")
                                                      .location("Dock")
                                                      .rssi(-40.0)
                                                      .readAt(now)
                                                      .build());

        assertThat(created.id()).isNotNull();
        assertThat(created.readAt()).isEqualTo(now);

        ArgumentCaptor<TagRead> savedCaptor = ArgumentCaptor.forClass(TagRead.class);
        verify(repository).save(savedCaptor.capture());
        assertThat(savedCaptor.getValue()
                              .epc()).isEqualTo("EPC1");

        verify(cloudLogger).log(eq("tag_read_created"), anyMap());
        verify(metricsPublisher).increment(eq("tag_reads.created"), eq(1.0), anyMap());
    }

    @Test
    void create_shouldDefaultReadAtWhenNull() {
        stubCloudAvailable();
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TagRead created = service.create(TagReadCreate.builder()
                                                      .siteName("SiteA")
                                                      .epc("EPC1")
                                                      .referenceCode("REF1")
                                                      .location("Dock")
                                                      .rssi(-40.0)
                                                      .readAt(null)
                                                      .build());

        assertThat(created.readAt()).isNotNull();
    }

    @Test
    void create_shouldSkipCloudWhenUnavailable() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cloudFactoryProvider.getIfAvailable()).thenReturn(null);

        TagRead created = service.create(TagReadCreate.builder()
                                        .siteName("SiteA")
                                        .epc("EPC1")
                                        .referenceCode("REF1")
                                        .location("Dock")
                                        .rssi(-40.0)
                                        .readAt(null)
                                        .build());

        assertThat(created.id()).isNotNull();
        verify(repository).save(any(TagRead.class));
        verifyNoInteractions(cloudLogger, metricsPublisher);
    }

    @Test
    void update_shouldPersistChangesAndRetainReadAtWhenNull() {
        UUID id = UUID.randomUUID();
        Instant originalReadAt = Instant.parse("2024-01-01T00:00:00Z");
        TagRead existing = TagRead.builder()
                                  .id(id)
                                  .siteName("SiteA")
                                  .epc("EPC1")
                                  .referenceCode("REF1")
                                  .location("Dock1")
                                  .rssi(-50.0)
                                  .readAt(originalReadAt)
                                  .build();

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        stubCloudAvailable();

        TagRead updated = service.update(id,
                                         TagReadUpdate.builder()
                                                      .siteName("SiteB")
                                                      .epc("EPC2")
                                                      .referenceCode("REF2")
                                                      .location("Dock2")
                                                      .rssi(-30.0)
                                                      .readAt(null)
                                                      .build());

        assertThat(updated.siteName()).isEqualTo("SiteB");
        assertThat(updated.epc()).isEqualTo("EPC2");
        assertThat(updated.readAt()).isEqualTo(originalReadAt); // retained when
                                                               // null

        verify(repository).save(any(TagRead.class));
        verify(cloudLogger).log(eq("tag_read_updated"), anyMap());
        verify(metricsPublisher).increment(eq("tag_reads.updated"), eq(1.0), anyMap());
    }

    @Test
    void update_shouldUseProvidedReadAtWhenNotNull() {
        UUID id = UUID.randomUUID();
        Instant supplied = Instant.parse("2024-02-02T00:00:00Z");
        TagRead existing = TagRead.builder()
                                  .id(id)
                                  .siteName("SiteA")
                                  .epc("EPC1")
                                  .referenceCode("REF1")
                                  .location("Dock1")
                                  .rssi(-50.0)
                                  .readAt(Instant.parse("2024-01-01T00:00:00Z"))
                                  .build();

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        stubCloudAvailable();

        TagRead updated = service.update(id,
                                         TagReadUpdate.builder()
                                                      .siteName("SiteB")
                                                      .epc("EPC2")
                                                      .referenceCode("REF2")
                                                      .location("Dock2")
                                                      .rssi(-30.0)
                                                      .readAt(supplied)
                                                      .build());

        assertThat(updated.readAt()).isEqualTo(supplied);
    }

    @Test
    void update_shouldWorkWhenCloudUnavailable() {
        UUID id = UUID.randomUUID();
        TagRead existing = TagRead.builder()
                                  .id(id)
                                  .siteName("SiteA")
                                  .epc("EPC1")
                                  .referenceCode("REF1")
                                  .location("Dock1")
                                  .rssi(-50.0)
                                  .readAt(Instant.parse("2024-01-01T00:00:00Z"))
                                  .build();

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cloudFactoryProvider.getIfAvailable()).thenReturn(null);

        TagRead updated = service.update(id,
                                         TagReadUpdate.builder()
                                                      .siteName("SiteB")
                                                      .epc("EPC2")
                                                      .referenceCode("REF2")
                                                      .location("Dock2")
                                                      .rssi(-30.0)
                                                      .readAt(Instant.parse("2024-02-02T00:00:00Z"))
                                                      .build());

        assertThat(updated.siteName()).isEqualTo("SiteB");
        verify(repository).save(any(TagRead.class));
        verifyNoInteractions(cloudLogger, metricsPublisher);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id)).isInstanceOf(TagReadNotFoundException.class);
    }

    @Test
    void delete_shouldRemoveAndEmitMetrics() {
        UUID id = UUID.randomUUID();
        TagRead existing = TagRead.builder()
                                  .id(id)
                                  .build();
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        stubCloudAvailable();

        service.delete(id);

        verify(repository).deleteById(id);
        verify(cloudLogger).log(eq("tag_read_deleted"), anyMap());
        verify(metricsPublisher).increment(eq("tag_reads.deleted"), eq(1.0), anyMap());
    }

    @Test
    void delete_shouldWorkWhenCloudUnavailable() {
        UUID id = UUID.randomUUID();
        TagRead existing = TagRead.builder()
                                  .id(id)
                                  .build();
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(cloudFactoryProvider.getIfAvailable()).thenReturn(null);

        service.delete(id);

        verify(repository).deleteById(id);
        verifyNoInteractions(cloudLogger, metricsPublisher);
    }

    @Test
    void findByFilters_shouldDelegateToRepository() {
        when(repository.findByFilters(Optional.of("E"), Optional.empty(), Optional.of("S"))).thenReturn(List.of());

        List<TagRead> results = service.findByFilters(Optional.of("E"), Optional.empty(), Optional.of("S"));

        assertThat(results).isEmpty();
        verify(repository).findByFilters(Optional.of("E"), Optional.empty(), Optional.of("S"));
    }

    private void stubCloudAvailable() {
        when(cloudFactoryProvider.getIfAvailable()).thenReturn(cloudFactory);
        when(cloudFactory.logger()).thenReturn(Optional.of(cloudLogger));
        when(cloudFactory.metrics()).thenReturn(Optional.of(metricsPublisher));
    }
}
