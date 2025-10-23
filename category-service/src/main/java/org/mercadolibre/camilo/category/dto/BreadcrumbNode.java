package org.mercadolibre.camilo.category.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BreadcrumbNode {
    String id;
    String name;
}
