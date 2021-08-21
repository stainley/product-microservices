package com.salapp.recommendation.services;

import com.salapp.api.core.recommendation.Recommendation;
import com.salapp.recommendation.model.RecommendationEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

public class MapperTests {

    private RecommendationMapper mapper = Mappers.getMapper(RecommendationMapper.class);

    @Test
    void mapperTests() {

        Assertions.assertNotNull(mapper);

        Recommendation api = new Recommendation(1, 2, "a", 4, "C", "addr");

        RecommendationEntity entity = mapper.apiToEntity(api);

        Assertions.assertEquals(api.getProductId(), entity.getProductId());
        Assertions.assertEquals(api.getRecommendationId(), entity.getRecommendationId());
        Assertions.assertEquals(api.getAuthor(), entity.getAuthor());
        Assertions.assertEquals(api.getRate(), entity.getRating());
        Assertions.assertEquals(api.getContent(), entity.getContent());

        Recommendation api2 = mapper.entityToApi(entity);

        Assertions.assertEquals(api.getProductId(), api2.getProductId());
        Assertions.assertEquals(api.getRecommendationId(), api2.getRecommendationId());
        Assertions.assertEquals(api.getAuthor(), api2.getAuthor());
        Assertions.assertEquals(api.getContent(), api2.getContent());
        Assertions.assertNull(api2.getServiceAddress());

    }

    @Test
    void mapperListTests() {
        Assertions.assertNotNull(mapper);

        Recommendation api = new Recommendation(1, 2, "a", 4, "C", "addr");
        List<Recommendation> apiList = Collections.singletonList(api);

        List<RecommendationEntity> entityList = mapper.apiListToEntityList(apiList);
        Assertions.assertEquals(apiList.size(), entityList.size());

        RecommendationEntity entity = entityList.get(0);

        Assertions.assertEquals(api.getProductId(), entity.getProductId());
        Assertions.assertEquals(api.getRecommendationId(), entity.getRecommendationId());
        Assertions.assertEquals(api.getAuthor(), entity.getAuthor());
        Assertions.assertEquals(api.getRate(), entity.getRating());
        Assertions.assertEquals(api.getContent(), entity.getContent());

        List<Recommendation> api2List = mapper.entityListToApiList(entityList);
        Assertions.assertEquals(apiList.size(), api2List.size());

        Recommendation api2 = api2List.get(0);

        Assertions.assertEquals(api.getProductId(), api2.getProductId());
        Assertions.assertEquals(api.getRecommendationId(), api2.getRecommendationId());
        Assertions.assertEquals(api.getAuthor(), api2.getAuthor());
        Assertions.assertEquals(api.getRate(), api2.getRate());
        Assertions.assertEquals(api.getContent(), api2.getContent());
        Assertions.assertNull(api2.getServiceAddress());

    }

}


