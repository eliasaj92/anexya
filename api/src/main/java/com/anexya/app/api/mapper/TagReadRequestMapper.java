package com.anexya.app.api.mapper;

import org.mapstruct.Mapper;

import com.anexya.app.api.CreateTagReadRequest;
import com.anexya.app.api.UpdateTagReadRequest;
import com.anexya.app.service.model.TagReadCreate;
import com.anexya.app.service.model.TagReadUpdate;

@Mapper(componentModel = "spring")
public interface TagReadRequestMapper {
    TagReadCreate toCreate(CreateTagReadRequest request);

    TagReadUpdate toUpdate(UpdateTagReadRequest request);
}
