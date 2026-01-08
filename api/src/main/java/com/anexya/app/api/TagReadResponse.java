package com.anexya.app.api;

import java.time.Instant;
import java.util.UUID;

import com.anexya.app.domain.TagRead;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TagReadResponse
{
    UUID id;
    String siteName;
    String epc;
    String referenceCode;
    String location;
    Double rssi;
    Instant readAt;

    public static TagReadResponse from(TagRead tx)
    {
        return TagReadResponse.builder().id(tx.getId()).siteName(tx.getSiteName()).epc(tx.getEpc())
                .referenceCode(tx.getReferenceCode()).location(tx.getLocation()).rssi(tx.getRssi())
                .readAt(tx.getReadAt()).build();
    }
}
