package com.anexya.app.api.mapper;

import org.springframework.stereotype.Component;

import com.anexya.app.api.TagSummaryResponse;
import com.anexya.app.service.TagSummary;

@Component
public class TagSummaryMapper
{

    public TagSummaryResponse toResponse(TagSummary summary)
    {
        if (summary == null)
        {
            return null;
        }

        return new TagSummaryResponse(summary.epc(), summary.totalReadCount(), summary.averageRssi(),
                summary.peakRssi(), summary.locationCount(), summary.mostDetectedLocation(),
                summary.firstSeen() != null ? summary.firstSeen().toString() : null,
                summary.lastSeen() != null ? summary.lastSeen().toString() : null);
    }
}
