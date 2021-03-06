package com.salapp.composite.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salapp.api.core.product.Product;
import com.salapp.api.core.product.ProductService;
import com.salapp.api.core.recommendation.Recommendation;
import com.salapp.api.core.recommendation.RecommendationService;
import com.salapp.api.core.review.Review;
import com.salapp.api.core.review.ReviewService;
import com.salapp.util.exceptions.InvalidInputException;
import com.salapp.util.exceptions.NotFoundException;
import com.salapp.util.http.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private static final String HTTP = "http://";
    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    @Autowired
    public ProductCompositeIntegration(
            RestTemplate restTemplate,
            ObjectMapper mapper,
            @Value("${app.product-service.host}") String productServiceUrl,
            @Value("${app.product-service.port}") int productServicePort,

            @Value("${app.recommendation-service.host}") String recommendationServiceUrl,
            @Value("${app.recommendation-service.port}") int recommendationServicePort,

            @Value("${app.review-service.host}") String reviewServiceUrl,
            @Value("${app.review-service.port}") int reviewServicePort
    ) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;

        this.productServiceUrl = HTTP + productServiceUrl + ":" + productServicePort + "/product/";
        this.recommendationServiceUrl = HTTP + recommendationServiceUrl + ":" + recommendationServicePort + "/recommendation?productId=";
        this.reviewServiceUrl = HTTP + reviewServiceUrl + ":" + reviewServicePort + "/review?productId=";
    }

    @Override
    public Product createProduct(Product body) {

        try {
            String url = productServiceUrl;
            LOG.debug("Will post a new product to URL: {}", url);

            Product product = restTemplate.postForObject(url, body, Product.class);
            LOG.debug("Created a product with id: {}", product != null ? product.getProductId() : "");

            return product;
        } catch (HttpClientErrorException e) {
            throw handleHttpClientException(e);
        }
    }

    @Override
    public Product getProduct(int productId) {
        Product product;

        try {
            String url = productServiceUrl + productId;
            LOG.debug("Will call getProduct API on URL: {}", url);

            product = restTemplate.getForObject(url, Product.class);

            assert product != null;
            LOG.debug("Found a product with id: {}", Optional.of(product.getProductId()).get());

            return product;
        } catch (HttpClientErrorException ex) {
            switch (ex.getStatusCode()) {
                case NOT_FOUND:
                    throw new NotFoundException(getErrorMessage(ex));
                case UNPROCESSABLE_ENTITY:
                    throw new InvalidInputException(getErrorMessage(ex));
                default:
                    LOG.warn("Got a unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
                    LOG.warn("Error body: {}", ex.getResponseBodyAsString());
                    throw ex;
            }
        }
    }

    @Override
    public void deleteProduct(int productId) {
        try {
            String url = productServiceUrl + "/" + productId;
            LOG.debug("Will call the deleteProduct API on URL: {}", url);

            restTemplate.delete(url);
        } catch (HttpClientErrorException exception) {
            throw handleHttpClientException(exception);
        }
    }


    @Override
    public Recommendation createRecommendation(Recommendation body) {
        try {
            String url = recommendationServiceUrl;
            LOG.debug("Will post a new recommendation to URL: {}", url);

            Recommendation recommendation = restTemplate.postForObject(url, body, Recommendation.class);
            LOG.debug("Created a recommendation with id: {}", recommendation != null ? recommendation.getProductId() : "");

            return recommendation;
        } catch (HttpClientErrorException exception) {
            throw handleHttpClientException(exception);
        }
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        try {
            String url = recommendationServiceUrl + productId;

            LOG.debug("Will call getRecommendations API on URL: {}", url);
            List<Recommendation> recommendations;
            recommendations = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Recommendation>>() {
            }).getBody();

            LOG.debug("Found {} recommendations for a product with id: {}", recommendations != null ? recommendations.size() : 0, productId);
            return recommendations;
        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting recommendations, return zero recommendations: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteRecommendations(int productId) {
        try {
            String url = recommendationServiceUrl + productId;
            LOG.debug("Will call the deleteRecommendation API on URL: {}", url);

            restTemplate.delete(url);
        } catch (HttpClientErrorException exception) {
            throw handleHttpClientException(exception);
        }
    }

    @Override
    public Review createReview(Review body) {
        try {
            String url = reviewServiceUrl;
            LOG.debug("Will post a new review to URL: {}", url);

            Review review = restTemplate.postForObject(url, body, Review.class);
            LOG.debug("Created a review with id: {}", review != null ? review.getProductId() : "");

            return review;
        } catch (HttpClientErrorException e) {
            throw handleHttpClientException(e);
        }
    }

    @Override
    public List<Review> getReviews(int productId) {

        try {
            String url = reviewServiceUrl + productId;

            LOG.debug("Will call getReviews API on URL: {}", url);
            List<Review> reviews = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Review>>() {
            }).getBody();

            LOG.debug("Found {} reviews for a product with id: {}", reviews != null ? reviews.size() : 0, productId);
            return reviews;

        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting recommendations, return zero recommendations: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteReviews(int productId) {
        try {
            String url = reviewServiceUrl + productId;
            LOG.debug("Will call the deleteReviews API on URL: {}", url);

            restTemplate.delete(url);

        } catch (HttpClientErrorException exception) {
            throw handleHttpClientException(exception);
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException exception) {

        switch (exception.getStatusCode()) {
            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(exception));
            case UNPROCESSABLE_ENTITY:
                return new InvalidInputException(getErrorMessage(exception));
            default:
                LOG.warn("Got a unexpected HTTP error: {}, will rethrow it", exception.getStatusCode());
                LOG.warn("Error body: {}", exception.getResponseBodyAsString());
                return exception;
        }
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (Exception iex) {
            return iex.getMessage();
        }
    }
}
