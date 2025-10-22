package org.mercadolibre.camilo.search.service.facade.categories.model;

import lombok.Value;

import java.util.List;

@Value
public class CategoryResponse {
    String id;
    String name;
    String parentId;
    List<BreadcrumbNode> pathFromRoot;
    int childrenCount;

    @Value
    public static class BreadcrumbNode {
        String id;
        String name;
    }
}
