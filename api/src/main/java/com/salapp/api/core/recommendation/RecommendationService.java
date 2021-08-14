package com.salapp.api.core.recommendation;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface RecommendationService {

    /**
     * curl $HOST:$PORT/recommendation?productId=1
     * @param productId
     * @return
     */
    @GetMapping(value = "/recommendation", produces = MediaType.APPLICATION_JSON_VALUE)
    List<Recommendation> getRecommendations(@RequestParam(value = "productId") int productId);
}
