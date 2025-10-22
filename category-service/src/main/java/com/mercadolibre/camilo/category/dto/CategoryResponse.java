package com.mercadolibre.camilo.category.dto;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class CategoryResponse {
    String id;
    String name;
    String parentId;
    @Singular("crumb")
    List<BreadcrumbNode> pathFromRoot;
    int childrenCount;
}
