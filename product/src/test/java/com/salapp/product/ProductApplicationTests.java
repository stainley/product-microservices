package com.salapp.product;

import com.salapp.api.core.product.Product;
import com.salapp.product.model.ProductEntity;
import com.salapp.product.repositories.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.http.HttpStatus.*;
import static reactor.core.publisher.Mono.just;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
class ProductApplicationTests {

    @Autowired
    private WebTestClient client;

    @Autowired
    private ProductRepository repository;

    @Test
    void contextLoads() {
    }

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    public void getProductById() {
        int productId = 1;

        /*when(repository.findByProductId(Mockito.anyInt()))
                .thenReturn(Optional.of(new ProductEntity(1, "s", 1)));*/


        postAndVerifyProduct(productId, OK);

        Assertions.assertTrue(repository.findByProductId(productId).isPresent());

        getAndVerifyProduct(productId, OK)
                .jsonPath("$.productId").isEqualTo(productId);

    }

    @Test
    void duplicateError() {
        int productId = 1;
        postAndVerifyProduct(productId, OK);

        postAndVerifyProduct(productId, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/product")
                .jsonPath("$.message").isEqualTo("Duplicate key, Product Id: " + productId);
    }

    @Test
    @DisplayName("when product is deleted")
    void deleteProduct() {
        int productId = 1;
        postAndVerifyProduct(productId, OK);

        deleteAndVerifyProduct(productId, OK);
        Assertions.assertFalse(repository.findByProductId(productId).isPresent());

        deleteAndVerifyProduct(productId, OK);
    }

    @Test
    public void getProductInvalidParameterString() {

        getAndVerifyProduct("/no-integer", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/product/no-integer")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    public void getProductNotFound() {
        int productIdNotFound = 13;

        getAndVerifyProduct(productIdNotFound, NOT_FOUND)
                .jsonPath("$.path").isEqualTo("/product/" + productIdNotFound)
                .jsonPath("$.message").isEqualTo("No product found for productId: " + productIdNotFound);
    }

    @Test
    public void getProductInvalidParameterNegativeValue() {
        int productIdInvalid = -1;

        getAndVerifyProduct(productIdInvalid, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/product/" + productIdInvalid)
                .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        return getAndVerifyProduct("/" + productId, expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus) {
        return client.get()
                .uri("/product" + productIdPath)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec postAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        Product product = new Product(productId, "Name " + productId, productId, "SA");
        return client.post()
                .uri("/product")
                .body(just(product), Product.class)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        return client.delete()
                .uri("/product/" + productId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody();
    }

}
