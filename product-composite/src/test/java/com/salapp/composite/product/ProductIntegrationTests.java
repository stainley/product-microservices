package com.salapp.composite.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salapp.api.core.product.Product;
import com.salapp.api.core.recommendation.Recommendation;
import com.salapp.api.core.review.Review;
import com.salapp.composite.product.services.ProductCompositeIntegration;
import com.salapp.util.exceptions.InvalidInputException;
import com.salapp.util.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.http.HttpTimeoutException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductIntegrationTests {

    private static final int PRODUCT_ID_OK = 1;
    private static final int PRODUCT_ID_NOT_FOUND = -13;
    private static final int PRODUCT_ID_INVALID = -1;


    @Autowired
    private ProductCompositeIntegration integration;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {

        new ProductCompositeIntegration(restTemplate, mapper,
                "http://localhost", 7001,
                "http://recommendation", 7002,
                "http://review", 7003
        );
    }

    @Test
    @ExceptionHandler(value = HttpClientErrorException.class)
    void getProduct() {

        Product productReturned = integration.getProduct(PRODUCT_ID_OK);
        assertThat(productReturned.getName()).isEqualTo("name-1");
    }

    @Test
    void getRecommendations() {
        Recommendation recommendation = integration.getRecommendations(1).get(1);

        assertThat(recommendation.getAuthor()).isEqualTo("Author 2");
    }

    @Test
    void getReviews() {
        Review review = integration.getReviews(1).get(1);
        assertThat(review.getAuthor()).isEqualTo("Author 2");
    }


    @Nested
    class NestedExceptionClass {

        @Test
        @ExceptionHandler(value = HttpClientErrorException.class)
        void getProductNotFound() {
            try {
                integration.getProduct(13).getProductId();
            } catch (NotFoundException nfe) {
                assertThat(nfe).isInstanceOf(NotFoundException.class);
            }
        }

        @Test
        @ExceptionHandler(value = HttpClientErrorException.class)
        void getProductInvalid() {
            try {
                integration.getProduct(-1).getName();
            } catch (InvalidInputException iie) {
                assertThat(iie).isInstanceOf(InvalidInputException.class);
            }
        }

        @Test
        @ExceptionHandler(value = HttpClientErrorException.class)
        void getProductUnknownException() {
            try {
                integration.getProduct(500).getName();
            } catch (Exception e) {
                assertThat(e).isInstanceOf(HttpServerErrorException.InternalServerError.class);
            }
        }

        @Test
        void getRecommendationException() {
            try {
                integration.getRecommendations(13).get(1).getAuthor();
            } catch (Exception e) {
                assertThat(e).isInstanceOf(HttpServerErrorException.InternalServerError.class);
            }
        }
    }

}
