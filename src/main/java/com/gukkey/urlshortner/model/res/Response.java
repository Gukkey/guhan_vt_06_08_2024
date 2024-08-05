package com.gukkey.urlshortner.model.res;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class Response {
    protected int status;
    protected String message;
    protected String shortURL;
    protected String destinationURL;
    protected LocalDate expireAt;
}
