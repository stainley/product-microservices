package com.salapp.product;

import com.salapp.product.model.ProductEntity;
import com.salapp.product.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
class ProductApplicationTests {

    @Autowired
    private WebTestClient client;

    @MockBean
    private ProductRepository repository;

    @Test
    void contextLoads() {

    }

    @BeforeEach
    void setUp() {

    }

    @Test
    public void getProductById() {
        int productId = 1;

        when(repository.findByProductId(Mockito.anyInt()))
                .thenReturn(Optional.of(new ProductEntity(1, "s", 1)));


        client.get()
                .uri("/product/" + productId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.productId").isEqualTo(productId);
    }

    @Test
    public void getProductInvalidParameterString() {

        client.get()
                .uri("/product/no-integer")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(BAD_REQUEST)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.path").isEqualTo("/product/no-integer")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    public void getProductNotFound() {
        int productIdNotFound = 13;

        when(repository.findByProductId(Mockito.anyInt()))
                .thenReturn(Optional.empty());


        client.get()
                .uri("/product/" + productIdNotFound)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.path").isEqualTo("/product/" + productIdNotFound)
                .jsonPath("$.message").isEqualTo("No product found for productId: " + productIdNotFound);
    }

    @Test
    public void getProductInvalidParameterNegativeValue() {
        int productIdInvalid = -1;


        client.get()
                .uri("/product/" + productIdInvalid)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.path").isEqualTo("/product/" + productIdInvalid)
                .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
    }

}
