package org.mercadolibre.camilo.qa.exception;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorCodes {
    private static final String PREFIX = "QA-01-";
    public static final String UNKNOWN_ERROR = PREFIX + "000";
    public static final String INVALID_REQUEST = PREFIX + "001";
    public static final String QUESTION_NOT_FOUND = PREFIX + "002";
}

