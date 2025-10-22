package com.mercadolibre.camilo.products.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Attribute {
    String name;
    String value;
}