package com.mercadolibre.camilo.review.exception;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorCodes {
    private static final String PREFIX = "RV-01-";
    public static final String UNKNOWN_ERROR = PREFIX + "000";
    public static final String INVALID_REQUEST = PREFIX + "001";
}
