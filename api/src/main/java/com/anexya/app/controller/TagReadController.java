package com.anexya.app.controller;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.anexya.app.api.CreateTagReadRequest;
import com.anexya.app.api.TagReadResponse;
import com.anexya.app.api.TagSummaryResponse;
import com.anexya.app.service.AggregationStrategy;
import com.anexya.app.service.TagReadService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tag-reads")
@RequiredArgsConstructor
@Validated
public class TagReadController {

    private final TagReadService tagReadService;
    private final AggregationStrategy aggregationStrategy;

    @GetMapping
    public List<TagReadResponse> list() {
        return tagReadService.findAll().stream().map(TagReadResponse::from).toList();
    }

    @GetMapping("/{id}")
    public TagReadResponse get(@PathVariable UUID id) {
        return TagReadResponse.from(tagReadService.get(id));
    }

    @PostMapping
    public ResponseEntity<TagReadResponse> create(@Valid @RequestBody CreateTagReadRequest request) {
        var created = tagReadService.create(
                request.getSiteName(),
                request.getEpc(),
                request.getReferenceCode(),
                request.getLocation(),
                request.getRssi(),
                request.getReadAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(TagReadResponse.from(created));
    }

    @GetMapping("/summary/by-epc")
    public List<TagSummaryResponse> summarizeByEpc(@RequestParam("startDate") Instant startDate,
                                                   @RequestParam("endDate") Instant endDate,
                                                   @RequestParam(value = "siteName", required = false) String siteName,
                                                   @RequestParam(value = "epc", required = false) String epc) {
        return aggregationStrategy
                .summarizeByTag(startDate, endDate, Optional.ofNullable(siteName), Optional.ofNullable(epc))
                .stream()
                .map(TagSummaryResponse::from)
                .toList();
    }
}
