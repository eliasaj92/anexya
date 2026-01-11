package com.anexya.app.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.anexya.app.api.TagSummaryResponse;
import com.anexya.app.service.model.TagSummary;

@Mapper(componentModel = "spring")
public interface TagSummaryMapper {
    @Mapping(target = "firstSeen", expression = "java(summary.firstSeen() != null ? summary.firstSeen().toString() : null)")
    @Mapping(target = "lastSeen", expression = "java(summary.lastSeen() != null ? summary.lastSeen().toString() : null)")
    TagSummaryResponse toResponse(TagSummary summary);
}
