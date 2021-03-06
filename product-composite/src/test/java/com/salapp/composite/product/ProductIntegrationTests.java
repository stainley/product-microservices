package com.salapp.composite.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salapp.api.core.product.Product;
import com.salapp.api.core.recommendation.Recommendation;
import com.salapp.api.core.review.Review;
import com.salapp.composite.product.services.ProductCompositeIntegration;
import com.salapp.util.exceptions.InvalidInputException;
import com.salapp.util.exceptions.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductIntegrationTests {

    private static final int PRODUCT_ID_OK = 1;
    private static final int PRODUCT_ID_NOT_FOUND = -13;
    private static final int PRODUCT_ID_INVALID = -1;
    private static final String URL = "http://localhost:";

    @Autowired
    private ProductCompositeIntegration integration;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper = new ObjectMapper();

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);

        new ProductCompositeIntegration(restTemplate, mapper,
                URL, 7001,
                URL, 7002,
                URL, 7003
        );
    }

    @Nested
    class RecommendationsTestSuite {

        @Test
        void getRecommendations() throws Exception {
            List<Recommendation> recommendations = Collections.singletonList(new Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mock-address"));

            mockServer.expect(
                            ExpectedCount.once(),
                            requestTo(new URI(URL + "7002/recommendation?productId=" + PRODUCT_ID_OK)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(
                            withStatus(HttpStatus.OK)
                                    .contentType(APPLICATION_JSON)
                                    .body(mapper.writeValueAsString(recommendations))
                    );

            List<Recommendation> returnedRecommendations = integration.getRecommendations(PRODUCT_ID_OK);
            mockServer.verify();

            assertThat(returnedRecommendations.get(0).getAuthor()).isEqualTo("author");
        }

        @Test
        void createRecommendation() throws Exception {

            Recommendation newRecommendation = new Recommendation(PRODUCT_ID_OK, 1, "Author " + PRODUCT_ID_OK, 1, "Content ", "SA");

            mockServer.expect(ExpectedCount.once(),
                            requestTo(new URI(URL + "7002/recommendation?productId=")))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withStatus(HttpStatus.OK)
                            .contentType(APPLICATION_JSON)
                            .body(mapper.writeValueAsString(newRecommendation))
                    );

            Recommendation recommendationCreated = integration.createRecommendation(newRecommendation);
            mockServer.verify();

            assertThat(recommendationCreated.getContent()).isEqualTo(newRecommendation.getContent());
        }

        @Test
        void createInvalidRecommendation() throws Exception {

            Recommendation newRecommendation = new Recommendation(PRODUCT_ID_OK, 1, "Author " + PRODUCT_ID_OK, 1, "Content ", "SA");

            mockServer.expect(ExpectedCount.once(),
                            requestTo(new URI(URL + "7002/recommendation?productId=")))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withStatus(HttpStatus.FORBIDDEN)
                            .contentType(APPLICATION_JSON)
                            .body(mapper.writeValueAsString(new Recommendation()))
                    );

            Exception exception = Assertions.assertThrows(HttpClientErrorException.Forbidden.class, () -> integration.createRecommendation(newRecommendation));
            mockServer.verify();

            assertThat(exception).isInstanceOf(HttpClientErrorException.Forbidden.class);
        }

        @Test
        void deleteRecommendation() throws Exception {

            mockServer.expect(
                            requestTo(new URI(URL + "7002/recommendation?productId=" + PRODUCT_ID_OK)))
                    .andExpect(method(HttpMethod.DELETE))
                    .andRespond(withStatus(HttpStatus.OK));

            integration.deleteRecommendations(PRODUCT_ID_OK);
            mockServer.verify();
        }

        @Test
        void deleteRecommendationNotFound() throws Exception {
            mockServer.expect(ExpectedCount.once(),
                            requestTo(new URI(URL + "7002/recommendation?productId=" + PRODUCT_ID_NOT_FOUND)))
                    .andExpect(method(HttpMethod.DELETE))
                    .andRespond(withStatus(HttpStatus.NOT_FOUND)
                            .contentType(APPLICATION_JSON)
                            .body(mapper.writeValueAsString(new Recommendation()))
                    );

            Exception exception = Assertions.assertThrows(NotFoundException.class, () -> integration.deleteRecommendations(PRODUCT_ID_NOT_FOUND));
            mockServer.verify();

            assertThat(exception).isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    class ProductTestSuite {
        @Test
        void createInvalidProduct() throws Exception {

            Product newProduct = new Product(PRODUCT_ID_INVALID, null, 1, "mock-address");

            mockServer.expect(ExpectedCount.once(),
                            requestTo(new URI(URL + "7001/product/")))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withStatus(HttpStatus.FORBIDDEN)
                            .contentType(APPLICATION_JSON)
                            .body(mapper.writeValueAsString(new Product()))
                    );

            Exception exception = Assertions.assertThrows(HttpClientErrorException.Forbidden.class, () -> integration.createProduct(newProduct));
            mockServer.verify();

            assertThat(exception).isInstanceOf(HttpClientErrorException.Forbidden.class);
        }


        @Test
        void getProduct() throws Exception {
            Product newProduct = new Product(PRODUCT_ID_OK, "name-mocked", 1, "mock-address");

            mockServer.expect(ExpectedCount.once(),
                            requestTo(new URI(URL + "7001/product/" + PRODUCT_ID_OK)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.OK)
                            .contentType(APPLICATION_JSON)
                            .body(mapper.writeValueAsString(newProduct)));

            Product productReturned = integration.getProduct(PRODUCT_ID_OK);
            mockServer.verify();

            assertThat(productReturned.getName()).isEqualTo("name-mocked");
        }

        @Test
        void createProduct() throws Exception {
            Product newProduct = new Product(PRODUCT_ID_OK, "name-mocked", 1, "mock-address");

            mockServer.expect(ExpectedCount.once(),
                            requestTo(new URI(URL + "7001/product/")))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withStatus(HttpStatus.OK)
                            .contentType(APPLICATION_JSON)
                            .body(mapper.writeValueAsString(newProduct)));

            Product productCreated = integration.createProduct(newProduct);
            mockServer.verify();

            assertThat(productCreated.getName()).isEqualTo("name-mocked");
        }

        @Test
        void deleteProduct() throws Exception {

            mockServer.expect(
                            requestTo(new URI(URL + "7001/product/" + PRODUCT_ID_OK)))
                    .andExpect(method(HttpMethod.DELETE))
                    .andRespond(withStatus(HttpStatus.OK));

            integration.deleteProduct(PRODUCT_ID_OK);
            mockServer.verify();

        }

        @Test
        void deleteProductNotFound() throws Exception {
            mockServer.expect(ExpectedCount.once(),
                            requestTo(new URI(URL + "7001/product/" + PRODUCT_ID_NOT_FOUND)))
                    .andExpect(method(HttpMethod.DELETE))
                    .andRespond(withStatus(HttpStatus.NOT_FOUND)
                            .contentType(APPLICATION_JSON)
                            .body(mapper.writeValueAsString(new Product()))
                    );

            Exception exception = Assertions.assertThrows(NotFoundException.class, () -> integration.deleteProduct(PRODUCT_ID_NOT_FOUND));
            mockServer.verify();

            assertThat(exception).isInstanceOf(NotFoundException.class);
        }
    }


    @Nested
    class ReviewTestSuite {
        @Test
        void getReviews() throws Exception {
            List<Review> reviews = Collections.singletonList(new Review(PRODUCT_ID_OK, 1, "author", "subject", "content", "mock-address"));

            mockServer.expect(
                            ExpectedCount.once(),
                            requestTo(new URI(URL + "7003/review?productId=" + PRODUCT_ID_OK)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.OK)
                            .contentType(APPLICATION_JSON)
                            .body(mapper.writeValueAsString(reviews))
                    );

            List<Review> reviewReturned = integration.getReviews(PRODUCT_ID_OK);
            mockServer.verify();
            assertThat(reviewReturned.size()).isEqualTo(1);
        }

        @Test
        void createReview() throws Exception {

            Review newReview = new Review(PRODUCT_ID_OK, 1, "Author", "Subject", "Content", "SA");

            mockServer.expect(ExpectedCount.once(),
                            requestTo(new URI(URL + "7003/review?productId=")))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withStatus(HttpStatus.OK)
                            .contentType(APPLICATION_JSON)
                            .body(mapper.writeValueAsString(newReview)));

            Review reviewCreated = integration.createReview(newReview);
            mockServer.verify();

            assertThat(reviewCreated.getAuthor()).isEqualTo("Author");
        }

        @Test
        void createInvalidReview() throws Exception {

            Review newReview = new Review(PRODUCT_ID_OK, 1, "Author", "Subject", "Content", "SA");

            mockServer.expect(ExpectedCount.once(),
                            requestTo(new URI(URL + "7003/review?productId=")))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                            .contentType(APPLICATION_JSON)
                            .body(mapper.writeValueAsString(newReview)));

            Exception exception = Assertions.assertThrows(InvalidInputException.class, () -> integration.createReview(newReview));
            mockServer.verify();

            assertThat(exception).isInstanceOf(InvalidInputException.class);
        }

        @Test
        void deleteReview() throws Exception {

            mockServer.expect(
                            requestTo(new URI(URL + "7003/review?productId=" + PRODUCT_ID_OK)))
                    .andExpect(method(HttpMethod.DELETE))
                    .andRespond(withStatus(HttpStatus.OK));

            integration.deleteReviews(PRODUCT_ID_OK);
            mockServer.verify();
        }

        @Test
        void deleteNotFoundReview() throws Exception {
            mockServer.expect(ExpectedCount.once(),
                            requestTo(new URI(URL + "7003/review?productId=" + PRODUCT_ID_NOT_FOUND)))
                    .andExpect(method(HttpMethod.DELETE))
                    .andRespond(withStatus(HttpStatus.NOT_FOUND)
                            .contentType(APPLICATION_JSON)
                            .body(mapper.writeValueAsString(new Review()))
                    );

            Exception exception = Assertions.assertThrows(NotFoundException.class, () -> integration.deleteReviews(PRODUCT_ID_NOT_FOUND));
            mockServer.verify();

            assertThat(exception).isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    class NestedExceptionClass {

        @Test
        @ExceptionHandler(value = HttpClientErrorException.class)
        void getProductNotFound() throws Exception {

            mockServer.expect(ExpectedCount.once(),
                            requestTo(new URI(URL + "7001/product/" + PRODUCT_ID_NOT_FOUND)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.NOT_FOUND)
                            .contentType(APPLICATION_JSON)
                            .body(mapper.writeValueAsString(new Product()))
                    );

            try {
                integration.getProduct(PRODUCT_ID_NOT_FOUND);
                mockServer.verify();
            } catch (NotFoundException nfe) {
                assertThat(nfe).isInstanceOf(NotFoundException.class);
            }
        }

        @Test
        @ExceptionHandler(value = HttpClientErrorException.class)
        void getProductInvalid() throws Exception {

            mockServer.expect(ExpectedCount.once(),
                            requestTo(new URI(URL + "7001/product/" + PRODUCT_ID_INVALID)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                            .contentType(APPLICATION_JSON)
                            .body(mapper.writeValueAsString(new Product()))
                    );

            try {
                integration.getProduct(PRODUCT_ID_INVALID);
                mockServer.verify();
            } catch (InvalidInputException nfe) {
                assertThat(nfe).isInstanceOf(InvalidInputException.class);
            }
        }

        @Test
        void getProductUnknownException() throws Exception {

            mockServer.expect(ExpectedCount.once(),
                            requestTo(new URI(URL + "7001/product/" + PRODUCT_ID_INVALID)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.FORBIDDEN)
                            .contentType(APPLICATION_JSON)
                            .body(mapper.writeValueAsString(new Product()))
                    );

            Exception exception = Assertions.assertThrows(HttpClientErrorException.Forbidden.class, () -> integration.getProduct(PRODUCT_ID_INVALID));
            mockServer.verify();

            assertThat(exception).isInstanceOf(HttpClientErrorException.Forbidden.class);
        }

        @Test
        void getRecommendationException() throws Exception {

            mockServer.expect(
                            ExpectedCount.once(),
                            requestTo(new URI(URL + "7002/recommendation?productId=" + PRODUCT_ID_NOT_FOUND)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(
                            withStatus(HttpStatus.NOT_FOUND)
                                    .contentType(APPLICATION_JSON)
                                    .body(mapper.writeValueAsString(Collections.singletonList(new Recommendation())))
                    );

            List<Recommendation> recommendations = integration.getRecommendations(PRODUCT_ID_NOT_FOUND);

            mockServer.verify();

            assertThat(recommendations.size()).isZero();
        }

        @Test
        void getReviewException() throws Exception {

            mockServer.expect(
                            ExpectedCount.once(),
                            requestTo(new URI(URL + "7003/review?productId=" + PRODUCT_ID_NOT_FOUND)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(
                            withStatus(HttpStatus.NOT_FOUND)
                                    .contentType(APPLICATION_JSON)
                                    .body(mapper.writeValueAsString(Collections.singletonList(new Recommendation())))
                    );

            List<Review> reviews = integration.getReviews(PRODUCT_ID_NOT_FOUND);
            mockServer.verify();

            assertThat(reviews.size()).isZero();
        }
    }

}
