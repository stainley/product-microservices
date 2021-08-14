package com.salapp.composite.product.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Collections;

import static springfox.documentation.builders.RequestHandlerSelectors.basePackage;

@Configuration
public class SpringFoxConfig {

    @Value("${api.common.version}")
    private String apiVersion;

    @Value("${api.common.title}")
    private String apiTitle;

    @Value("${api.common.description}")
    private String apiDescription;

    @Value("${api.common.termsOfServiceUrl}")
    private String apiTermsOfServiceUrl;

    @Value("${api.common.license}")
    private String apiLicense;

    @Value("${api.common.licenseUrl}")
    private String apiLicenseUrl;

    @Value("${api.common.contact.name}")
    private String apiContactName;

    @Value("${api.common.contact.url}")
    private String apiContactUrl;

    @Value("${api.common.contact.email}")
    private String apiContactEmail;

    /***
     * Will expose on $HOST:#PORT/swagger-ui.html
     * @return Docket
     */
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(basePackage("com.salapp.composite.product"))
                .paths(PathSelectors.any())
                .build()
                .globalResponses(HttpMethod.GET, Collections.emptyList())
                .apiInfo(new ApiInfo(
                        apiTitle,
                        apiDescription,
                        apiVersion,
                        apiTermsOfServiceUrl,
                        new Contact(apiContactName, apiContactUrl, apiContactEmail),
                        apiLicense,
                        apiLicenseUrl,
                        Collections.emptyList()
                ));
    }
}
