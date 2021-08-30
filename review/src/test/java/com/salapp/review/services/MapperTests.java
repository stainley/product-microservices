package com.salapp.review.services;

import com.salapp.api.core.review.Review;
import com.salapp.review.model.ReviewEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;


class MapperTests {
    private final ReviewMapper mapper = Mappers.getMapper(ReviewMapper.class);

    @Test
    void mapperTests() {

        Assertions.assertNotNull(mapper);

        Review api = new Review(1, 2, "a", "s", "C", "addr");

        ReviewEntity entity = mapper.apiToEntity(api);

        Assertions.assertEquals(api.getProductId(), entity.getProductId());
        Assertions.assertEquals(api.getReviewId(), entity.getReviewId());
        Assertions.assertEquals(api.getAuthor(), entity.getAuthor());
        Assertions.assertEquals(api.getSubject(), entity.getSubject());
        Assertions.assertEquals(api.getContent(), entity.getContent());

        Review api2 = mapper.entityToApi(entity);

        Assertions.assertEquals(api.getProductId(), api2.getProductId());
        Assertions.assertEquals(api.getReviewId(), api2.getReviewId());
        Assertions.assertEquals(api.getAuthor(), api2.getAuthor());
        Assertions.assertEquals(api.getSubject(), api2.getSubject());
        Assertions.assertEquals(api.getContent(), api2.getContent());
        Assertions.assertNull(api2.getServiceAddress());
    }

    @Test
    void mapperListTests() {
        Assertions.assertNotNull(mapper);

        Review api = new Review(1, 2, "a", "s", "C", "addr");
        List<Review> apiList = Collections.singletonList(api);

        List<ReviewEntity> entityList = mapper.apiListToEntityList(apiList);
        Assertions.assertEquals(apiList.size(), entityList.size());

        ReviewEntity entity = entityList.get(0);

        Assertions.assertEquals(api.getProductId(), entity.getProductId());
        Assertions.assertEquals(api.getReviewId(), entity.getReviewId());
        Assertions.assertEquals(api.getAuthor(), entity.getAuthor());
        Assertions.assertEquals(api.getSubject(), entity.getSubject());
        Assertions.assertEquals(api.getContent(), entity.getContent());

        List<Review> api2List = mapper.entityListToApiList(entityList);
        Assertions.assertEquals(apiList.size(), api2List.size());

        Review api2 = api2List.get(0);

        Assertions.assertEquals(api.getProductId(), api2.getProductId());
        Assertions.assertEquals(api.getReviewId(), api2.getReviewId());
        Assertions.assertEquals(api.getAuthor(), api2.getAuthor());
        Assertions.assertEquals(api.getSubject(), api2.getSubject());
        Assertions.assertEquals(api.getContent(), api2.getContent());
        Assertions.assertNull(api2.getServiceAddress());


    }
}
