package com.anexya.app.api.mapper;

import org.mapstruct.Mapper;

import com.anexya.app.api.TagReadResponse;
import com.anexya.app.domain.TagRead;

@Mapper(componentModel = "spring")
public interface TagReadMapper {
    TagReadResponse toResponse(TagRead tagRead);
}
