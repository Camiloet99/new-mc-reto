package com.mercadolibre.camilo.model;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class ErrorResponse {
    String code;
    String description;
    Integer httpStatus;
    String uri;
    Map<String, String> headers;
    String responseBody;
}
