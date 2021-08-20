package com.salapp.api.core.recommendation;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface RecommendationService {


    @PostMapping(value = "/recommendation", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Recommendation createRecommendation(@RequestBody Recommendation body);

    /**
     * curl $HOST:$PORT/recommendation?productId=1
     *
     * @param productId
     * @return
     */
    @GetMapping(value = "/recommendation", produces = MediaType.APPLICATION_JSON_VALUE)
    List<Recommendation> getRecommendations(@RequestParam(value = "productId") int productId);

    /**
     * curl -X DELETE $HOST:$PORT/recommendation?productId=1
     * @param productId
     */
    @DeleteMapping(value = "/recommendations")
    void deleteRecommendations(@RequestParam(value = "productId", required = true) int productId);
}
