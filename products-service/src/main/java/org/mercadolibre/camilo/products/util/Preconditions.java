package org.mercadolibre.camilo.products.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Preconditions {
    public static void requireNonBlank(String s, String msg) {
        if (s == null || s.isBlank()) throw new IllegalArgumentException(msg);
    }
}
