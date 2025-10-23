package org.mercadolibre.camilo.category.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Preconditions {

    public static void requireNonBlank(String s, String msg) {
        if (s == null || s.isBlank()) throw new IllegalArgumentException(msg);
    }

}
