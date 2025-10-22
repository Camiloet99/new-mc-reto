package org.mercadolibre.camilo.search.exception;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorCodes {

    private static final String PREFIX = "SS-01-";

    public static final String UNKNOWN_ERROR = PREFIX + "000";
    public static final String INVALID_REQUEST = PREFIX + "001";
    public static final String UPSTREAM_SERVICE_FAILURE = PREFIX + "002";
    public static final String ASYNC_REQUEST_TIMEOUT_ERROR = PREFIX + "003";

    public static final String PRODUCTS_INVALID_REQUEST = PREFIX + "100";
    public static final String PRODUCTS_NOT_FOUND = PREFIX + "101";
    public static final String PRODUCTS_UPSTREAM_FAILURE = PREFIX + "102";

    public static final String CATEGORIES_INVALID_REQUEST = PREFIX + "200";
    public static final String CATEGORIES_NOT_FOUND = PREFIX + "201";
    public static final String CATEGORIES_UPSTREAM_FAILURE = PREFIX + "202";

    public static final String SELLERS_INVALID_REQUEST = PREFIX + "300";
    public static final String SELLERS_NOT_FOUND = PREFIX + "301";
    public static final String SELLERS_UPSTREAM_FAILURE = PREFIX + "302";

    public static final String REVIEWS_INVALID_REQUEST = PREFIX + "400";
    public static final String REVIEWS_NOT_FOUND = PREFIX + "401";
    public static final String REVIEWS_UPSTREAM_FAILURE = PREFIX + "402";

    public static final String QA_INVALID_REQUEST = PREFIX + "500";
    public static final String QA_NOT_FOUND = PREFIX + "501";
    public static final String QA_UPSTREAM_FAILURE = PREFIX + "502";
}
