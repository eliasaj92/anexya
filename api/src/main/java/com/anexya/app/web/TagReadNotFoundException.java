package com.anexya.app.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TagReadNotFoundException extends RuntimeException
{
    public TagReadNotFoundException(UUID id)
    {
        super("Tag read not found: " + id);
    }
}
