package com.anexya.app.controller;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.anexya.app.api.CreateTagReadRequest;
import com.anexya.app.api.TagReadResponse;
import com.anexya.app.api.TagSummaryResponse;
import com.anexya.app.api.UpdateTagReadRequest;
import com.anexya.app.api.mapper.TagReadMapper;
import com.anexya.app.api.mapper.TagSummaryMapper;
import com.anexya.app.service.AggregationStrategy;
import com.anexya.app.service.TagReadService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tag-reads")
@RequiredArgsConstructor
@Validated
public class TagReadController
{

    private final TagReadService tagReadService;
    private final AggregationStrategy aggregationStrategy;
    private final TagReadMapper tagReadMapper;
    private final TagSummaryMapper tagSummaryMapper;

    @GetMapping("/{id}")
    public TagReadResponse get(@PathVariable UUID id)
    {
        return tagReadMapper.toResponse(tagReadService.get(id));
    }

    @GetMapping("/search")
    public List<TagReadResponse> search(@RequestParam(value = "epc", required = false) String epc,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "siteName", required = false) String siteName)
    {
        return tagReadService
                .findByFilters(Optional.ofNullable(epc), Optional.ofNullable(location), Optional.ofNullable(siteName))
                .stream().map(tagReadMapper::toResponse).toList();
    }

    @PostMapping
    public ResponseEntity<TagReadResponse> create(@Valid @RequestBody CreateTagReadRequest request)
    {
        var created = tagReadService.create(request.getSiteName(), request.getEpc(), request.getReferenceCode(),
                request.getLocation(), request.getRssi(), request.getReadAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(tagReadMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    public TagReadResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateTagReadRequest request)
    {
        var updated = tagReadService.update(id, request.getSiteName(), request.getEpc(), request.getReferenceCode(),
                request.getLocation(), request.getRssi(), request.getReadAt());
        return tagReadMapper.toResponse(updated);
    }

    @GetMapping("/summary/by-epc")
    public List<TagSummaryResponse> summarizeByEpc(@RequestParam("startDate") Instant startDate,
            @RequestParam("endDate") Instant endDate,
            @RequestParam(value = "siteName", required = false) String siteName,
            @RequestParam(value = "epc", required = false) String epc)
    {
        return aggregationStrategy
                .summarizeByTag(startDate, endDate, Optional.ofNullable(siteName), Optional.ofNullable(epc)).stream()
                .map(tagSummaryMapper::toResponse).toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id)
    {
        tagReadService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
