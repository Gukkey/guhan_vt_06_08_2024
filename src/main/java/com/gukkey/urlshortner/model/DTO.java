package com.gukkey.urlshortner.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class DTO {
    protected String destinationURL;
    protected long days;
}
