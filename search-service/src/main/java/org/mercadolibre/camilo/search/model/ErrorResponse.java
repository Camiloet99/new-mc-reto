package org.mercadolibre.camilo.search.model;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class ErrorResponse {
    String code;           // código interno, p.ej. SS-01-101
    String description;    // descripción legible
    Integer httpStatus;    // status devuelto por upstream (o el que propagamos)
    String uri;            // URI llamado a upstream
    Map<String, String> headers; // headers relevantes (si los incluimos)
    String responseBody;   // cuerpo crudo devuelto por upstream (si se quiere loguear/propagar)
}
