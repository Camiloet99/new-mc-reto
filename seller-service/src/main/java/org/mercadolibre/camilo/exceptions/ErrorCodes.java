package org.mercadolibre.camilo.exceptions;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorCodes {
    private static final String PREFIX = "SL-01-";

    public static final String UNKNOWN_ERROR = PREFIX + "000";
    public static final String INVALID_REQUEST = PREFIX + "001";
    public static final String SELLER_NOT_FOUND = PREFIX + "002";
}
