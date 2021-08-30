package com.salapp.api.core.review;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface ReviewService {

    /**
     * curl -X POST $HOST:$PORT/review -H "Content-Type: application/json --data '{productId:123, reviewId:456}'
     *
     * @param body
     * @return
     */

    @PostMapping(value = "/review", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Review createReview(@RequestBody Review body);

    @GetMapping(value = "/review", produces = MediaType.APPLICATION_JSON_VALUE)
    List<Review> getReviews(@RequestParam(value = "productId") int productId);

    @DeleteMapping("/review")
    void deleteReviews(@RequestParam(value = "productId", required = true) int productId);
}
