package com.anexya.app.api.mapper;

import org.springframework.stereotype.Component;

import com.anexya.app.api.TagReadResponse;
import com.anexya.app.domain.TagRead;

@Component
public class TagReadMapper
{

    public TagReadResponse toResponse(TagRead tagRead)
    {
        if (tagRead == null)
        {
            return null;
        }

        return TagReadResponse.builder().id(tagRead.getId()).siteName(tagRead.getSiteName()).epc(tagRead.getEpc())
                .referenceCode(tagRead.getReferenceCode()).location(tagRead.getLocation()).rssi(tagRead.getRssi())
                .readAt(tagRead.getReadAt()).build();
    }
}
