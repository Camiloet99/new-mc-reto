package org.mercadolibre.camilo.products.model;

public record Scored<T>(T value, double score) {
}
