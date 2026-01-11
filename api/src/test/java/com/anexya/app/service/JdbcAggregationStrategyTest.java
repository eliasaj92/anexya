package com.anexya.app.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.anexya.app.service.impl.JdbcAggregationStrategy;
import com.anexya.app.service.model.TagSummary;

@Testcontainers
@TestInstance(Lifecycle.PER_CLASS)
class JdbcAggregationStrategyTest {

    private static final String IMAGE = "mysql:8.0";

    @SuppressWarnings("resource")
    @Container
    private static final MySQLContainer<?> mysql = new MySQLContainer<>(IMAGE).withDatabaseName("testdb")
                                                                              .withUsername("test")
                                                                              .withPassword("test");

    private JdbcTemplate jdbcTemplate;
    private AggregationStrategy aggregationStrategy;

    @BeforeAll
    void setUpDataSource() {
        DataSource dataSource = new DriverManagerDataSource(Objects.requireNonNull(mysql.getJdbcUrl()),
                                                            Objects.requireNonNull(mysql.getUsername()),
                                                            Objects.requireNonNull(mysql.getPassword()));
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("drop table if exists tag_reads");
        jdbcTemplate.execute("""
                create table tag_reads (
                    id char(36) not null,
                    site_name varchar(255) not null,
                    epc varchar(255) not null,
                    reference_code varchar(1024) not null,
                    location varchar(255) not null,
                    rssi double not null,
                    read_at datetime(6) not null,
                    primary key (id)
                );
                """);
        aggregationStrategy = new JdbcAggregationStrategy(jdbcTemplate);
    }

    @BeforeEach
    void seed() {
        jdbcTemplate.execute("delete from tag_reads");

        insert("a1", "SiteA", "EPC1", "Dock1", -40.0, "2024-01-10T00:00:00Z");
        insert("a2", "SiteA", "EPC1", "Dock1", -42.0, "2024-01-11T00:00:00Z");
        insert("a3", "SiteA", "EPC1", "Dock2", -30.0, "2024-01-12T00:00:00Z");
        insert("b1", "SiteB", "EPC2", "Dock3", -20.0, "2024-01-13T00:00:00Z");
    }

    @AfterEach
    void clean() {
        jdbcTemplate.execute("delete from tag_reads");
    }

    @Test
    void summarize_noFilters_returnsAllEpcs() {
        List<TagSummary> summaries = aggregationStrategy.summarizeByTag(Instant.parse("2024-01-01T00:00:00Z"),
                                                                        Instant.parse("2024-02-01T00:00:00Z"),
                                                                        Optional.empty(),
                                                                        Optional.empty());

        summaries = summaries.stream()
                             .sorted(Comparator.comparing(TagSummary::epc))
                             .toList();

        assertThat(summaries).hasSize(2);

        TagSummary epc1 = summaries.get(0);
        assertThat(epc1.epc()).isEqualTo("EPC1");
        assertThat(epc1.totalReadCount()).isEqualTo(3);
        assertThat(epc1.averageRssi()).isEqualTo((-40.0 - 42.0 - 30.0) / 3.0);
        assertThat(epc1.peakRssi()).isEqualTo(-30.0);
        assertThat(epc1.locationCount()).isEqualTo(2);
        assertThat(epc1.mostDetectedLocation()).isEqualTo("Dock1");
        assertThat(epc1.firstSeen()).isEqualTo(Instant.parse("2024-01-10T00:00:00Z"));
        assertThat(epc1.lastSeen()).isEqualTo(Instant.parse("2024-01-12T00:00:00Z"));

        TagSummary epc2 = summaries.get(1);
        assertThat(epc2.epc()).isEqualTo("EPC2");
        assertThat(epc2.totalReadCount()).isEqualTo(1);
        assertThat(epc2.mostDetectedLocation()).isEqualTo("Dock3");
    }

    @Test
    void summarize_filtersBySite() {
        List<TagSummary> summaries = aggregationStrategy.summarizeByTag(Instant.parse("2024-01-01T00:00:00Z"),
                                                                        Instant.parse("2024-02-01T00:00:00Z"),
                                                                        Optional.of("SiteA"),
                                                                        Optional.empty());

        assertThat(summaries).hasSize(1);
        TagSummary only = summaries.get(0);
        assertThat(only.epc()).isEqualTo("EPC1");
        assertThat(only.locationCount()).isEqualTo(2);
    }

    @Test
    void summarize_filtersByEpc() {
        List<TagSummary> summaries = aggregationStrategy.summarizeByTag(Instant.parse("2024-01-01T00:00:00Z"),
                                                                        Instant.parse("2024-02-01T00:00:00Z"),
                                                                        Optional.empty(),
                                                                        Optional.of("EPC2"));

        assertThat(summaries).hasSize(1);
        assertThat(summaries.get(0)
                            .epc()).isEqualTo("EPC2");
        assertThat(summaries.get(0)
                            .totalReadCount()).isEqualTo(1);
    }

    private void insert(String id, String site, String epc, String location, double rssi, String isoInstant) {
        jdbcTemplate.update("insert into tag_reads (id, site_name, epc, reference_code, location, rssi, read_at) values (?,?,?,?,?,?,?)",
                            id,
                            site,
                            epc,
                            "REF-" + id,
                            location,
                            rssi,
                            Timestamp.from(Instant.parse(isoInstant)));
    }
}
