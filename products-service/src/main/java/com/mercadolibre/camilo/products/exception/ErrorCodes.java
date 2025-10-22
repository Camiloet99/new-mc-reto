package com.mercadolibre.camilo.products.exception;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorCodes {
    private static final String PREFIX = "PR-01-";

    public static final String UNKNOWN_ERROR = PREFIX + "000";
    public static final String INVALID_REQUEST = PREFIX + "001";
    public static final String PRODUCT_NOT_FOUND = PREFIX + "002";
}
