package com.salapp.recommendation.repositories;

import com.salapp.recommendation.model.RecommendationEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RecommendationRepository extends CrudRepository<RecommendationEntity, String> {

    List<RecommendationEntity> findByProductId(int productId);
}
