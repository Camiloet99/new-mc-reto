package org.mercadolibre.camilo.search.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "environment")
public class EnvironmentConfig {

    @NotBlank
    private String serviceName;

    private int maxPayloadSizeInMb;

    @NotNull
    private Boolean securityDisableSslCertValidation;

    @NotNull
    private Boolean logInvalidRequests;

    @Valid
    @NotNull
    private ServiceRetry serviceRetry;

    @Valid
    @NotNull
    private Domains domains;

    @Valid
    @NotNull
    private Http http;

    @Getter
    @Setter
    @Validated
    public static class ServiceRetry {
        @NotNull
        private Integer maxAttempts;
    }

    @Getter
    @Setter
    @Validated
    public static class Domains {
        @NotBlank
        private String productsBaseUrl;
        @NotBlank
        private String categoriesBaseUrl;
        @NotBlank
        private String sellersBaseUrl;
        @NotBlank
        private String reviewsBaseUrl;
        @NotBlank
        private String qaBaseUrl;
    }

    @Getter
    @Setter
    @Validated
    public static class Http {
        @NotNull
        @Min(500)
        @Max(60000)
        private Long timeoutMs;
    }
}
