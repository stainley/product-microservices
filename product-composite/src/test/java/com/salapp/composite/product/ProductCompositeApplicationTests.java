package com.salapp.composite.product;

import com.salapp.api.core.product.Product;
import com.salapp.api.core.recommendation.Recommendation;
import com.salapp.api.core.review.Review;
import com.salapp.composite.product.services.ProductCompositeIntegration;
import com.salapp.util.exceptions.InvalidInputException;
import com.salapp.util.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductCompositeApplicationTests {

    private static final int PRODUCT_ID_OK = 1;
    private static final int PRODUCT_ID_NOT_FOUND = 2;
    private static final int PRODUCT_ID_INVALID = 3;

    @Autowired
    private WebTestClient client;

    @MockBean
    private ProductCompositeIntegration compositeIntegration;

    @Test
    void contextLoads() {
    }

    @BeforeEach
    public void setUp() {
        when(compositeIntegration.getProduct(PRODUCT_ID_OK))
                .thenReturn(new Product(PRODUCT_ID_OK, "name", 1, "mock-address"));

        when(compositeIntegration.getRecommendations(PRODUCT_ID_OK))
                .thenReturn(Collections.singletonList(new Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mock-address")));

        when(compositeIntegration.getReviews(PRODUCT_ID_OK))
                .thenReturn(Collections.singletonList(new Review(PRODUCT_ID_OK, 1, "author", "subject", "content", "mock-address")));

        when(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
                .thenThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));

        when(compositeIntegration.getProduct(PRODUCT_ID_INVALID))
                .thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));
    }

    @Test
    public void getProductById() {
        client.get()
                .uri("/product-composite/" + PRODUCT_ID_OK)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
                .jsonPath("$.recommendations.length()").isEqualTo(1)
                .jsonPath("$.reviews.length()").isEqualTo(1);
    }

    @Test
    public void getProductNotFound() {
        client.get()
                .uri("/product-composite/" + PRODUCT_ID_NOT_FOUND)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
                .jsonPath("$.message").isEqualTo("NOT FOUND: " + PRODUCT_ID_NOT_FOUND);
    }

    @Test
    public void getProductInvalidInput() {
        client.get()
                .uri("/product-composite/" + PRODUCT_ID_INVALID)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_INVALID)
                .jsonPath("$.message").isEqualTo("INVALID: " + PRODUCT_ID_INVALID);
    }

}
