package com.anexya.app.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.anexya.app.domain.TagRead;

public interface TagReadService {
    List<TagRead> findAll();

    TagRead get(UUID id);

    TagRead create(String siteName, String epc, String referenceCode, String location, Double rssi, Instant readAt);
}
