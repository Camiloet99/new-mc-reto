package org.mercadolibre.camilo.products.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "Atributo o característica específica del producto")
public class Attribute {
    @Schema(description = "Nombre del atributo (ej. color, tamaño)")
    String name;

    @Schema(description = "Valor del atributo (ej. rojo, 42, algodón)")
    String value;
}