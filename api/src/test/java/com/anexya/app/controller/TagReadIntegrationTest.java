package com.anexya.app.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.anexya.app.AnexyaApplication;
import com.anexya.app.api.CreateTagReadRequest;
import com.anexya.app.api.TagReadResponse;
import com.anexya.app.api.UpdateTagReadRequest;

@SpringBootTest(classes = AnexyaApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("mysql")
@TestPropertySource(properties = "spring.flyway.locations=classpath:db/migration/testmysql")
@Testcontainers
class TagReadIntegrationTest {
    @SuppressWarnings("resource")
    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.2.0").withDatabaseName("tagreads")
                                                                              .withUsername("appuser")
                                                                              .withPassword("password");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
        registry.add("spring.flyway.enabled", () -> true);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Test
    void create_get_update_delete_roundTrip() {
        final Instant now = Instant.parse("2024-01-01T00:00:00Z");

        final CreateTagReadRequest createRequest = CreateTagReadRequest.builder()
                                                                       .siteName("Plant A")
                                                                       .epc("EPC123")
                                                                       .referenceCode("REF1")
                                                                       .location("Dock 1")
                                                                       .rssi(-45.5)
                                                                       .readAt(now)
                                                                       .build();

        final ResponseEntity<TagReadResponse> createResponse = restTemplate.postForEntity(baseUrl("/api/tag-reads"), createRequest, TagReadResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        final TagReadResponse createdBody = Objects.requireNonNull(createResponse.getBody());
        final UUID id = createdBody.id();

        final ResponseEntity<TagReadResponse> getResponse = restTemplate.getForEntity(baseUrl("/api/tag-reads/" + id), TagReadResponse.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        final TagReadResponse fetchedBody = Objects.requireNonNull(getResponse.getBody());
        assertThat(fetchedBody.epc()).isEqualTo("EPC123");

        final UpdateTagReadRequest updateRequest = UpdateTagReadRequest.builder()
                                                                       .siteName("Plant B")
                                                                       .epc("EPC123")
                                                                       .referenceCode("REF2")
                                                                       .location("Dock 2")
                                                                       .rssi(-40.0)
                                                                       .readAt(now.plusSeconds(60))
                                                                       .build();

        final HttpEntity<UpdateTagReadRequest> updateEntity = new HttpEntity<>(Objects.requireNonNull(updateRequest));
        final ResponseEntity<TagReadResponse> updateResponse = restTemplate.exchange(baseUrl("/api/tag-reads/" + id), HttpMethod.PUT, updateEntity, TagReadResponse.class);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        final TagReadResponse updatedBody = Objects.requireNonNull(updateResponse.getBody());
        assertThat(updatedBody.siteName()).isEqualTo("Plant B");
        assertThat(updatedBody.referenceCode()).isEqualTo("REF2");

        restTemplate.delete(baseUrl("/api/tag-reads/" + id));

        final ResponseEntity<String> afterDelete = restTemplate.getForEntity(baseUrl("/api/tag-reads/" + id), String.class);
        assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }

}
