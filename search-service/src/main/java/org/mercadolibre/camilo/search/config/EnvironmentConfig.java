package org.mercadolibre.camilo.search.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "environment")
public class EnvironmentConfig {

    @NotBlank
    private String marker;

    @NotBlank
    private String serviceName;

    @NotBlank
    private String shipCode;

    @Min(1)
    @Max(128)
    private int maxPayloadSizeInMb = 8;

    @NotNull
    private Boolean securityDisableSslCertValidation = false;

    @NotNull
    private Boolean logInvalidRequests = true;

    @NotBlank
    private String swaggerContext = "/swagger-ui";

    @NotNull
    @Size(min = 0)
    private List<@NotBlank String> headersForLogging;

    @Valid
    @NotNull
    private ServiceRetry serviceRetry = new ServiceRetry();

    @Valid
    @NotNull
    private Domains domains = new Domains();

    @Valid
    @NotNull
    private Http http = new Http();

    @Getter
    @Setter
    @Validated
    public static class ServiceRetry {
        @NotNull
        @Min(1)
        @Max(5)
        private Integer maxAttempts = 2;
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
        private Long timeoutMs = 3000L;
    }
}
