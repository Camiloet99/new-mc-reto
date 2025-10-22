package com.mercadolibre.camilo.category.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Category {
    String id;
    String name;
    String parentId;
}
