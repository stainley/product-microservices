package com.salapp.recommendation.repositories;

import com.salapp.recommendation.model.RecommendationEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class PersistenceTests {

    @Autowired
    private RecommendationRepository repository;

    private RecommendationEntity savedEntity;

    @BeforeEach
    public void setUpDb() {
        repository.deleteAll();

        RecommendationEntity entity = new RecommendationEntity(1, 2, "a", 3, "c");
        savedEntity = repository.save(entity);

        assertEqualsRecommendation(entity, savedEntity);
    }

    @Test
    void create() {
        RecommendationEntity newEntity = new RecommendationEntity(1, 3, "a", 3, "c");
        repository.save(newEntity);

        Optional<RecommendationEntity> foundEntity = repository.findById(newEntity.getId());

        foundEntity.ifPresent(entity -> assertEqualsRecommendation(newEntity, entity));
        Assertions.assertEquals(2, repository.count());
    }

    @Test
    void update() {
        savedEntity.setAuthor("a2");
        repository.save(savedEntity);

        Optional<RecommendationEntity> foundEntity = repository.findById(savedEntity.getId());

        foundEntity.ifPresent(entity -> Assertions.assertEquals(1, entity.getVersion()));
        foundEntity.ifPresent(entity -> Assertions.assertEquals("a2", foundEntity.get().getAuthor()));
    }

    @Test
    void delete() {
        repository.delete(savedEntity);
        Assertions.assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
    void getByProductId() {
        List<RecommendationEntity> entityList = repository.findByProductId(savedEntity.getProductId());

        assertThat(entityList).hasSize(1);
        assertEqualsRecommendation(savedEntity, entityList.get(0));
    }

    @Test
    void duplicateError() {
        RecommendationEntity entity = new RecommendationEntity(1, 2, "a", 3, "c");
        Exception exception = Assertions.assertThrows(DuplicateKeyException.class, () -> repository.save(entity));

        assertThat(exception).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void optimisticLockError() {
        Optional<RecommendationEntity> entity1 = repository.findById(savedEntity.getId());
        Optional<RecommendationEntity> entity2 = repository.findById(savedEntity.getId());

        try {
            entity1.ifPresent(entity -> {
                entity.setAuthor("a1");
                repository.save(entity1.get());
            });

            entity2.ifPresent(entity -> {
                entity2.get().setAuthor("a2");
                repository.save(entity2.get());
            });

        } catch (OptimisticLockingFailureException e) {
            Assertions.assertNotNull(e);
        }

        Optional<RecommendationEntity> updatedEntity = repository.findById(savedEntity.getId());

        if (updatedEntity.isPresent()) {
            Assertions.assertEquals(1, updatedEntity.get().getVersion());
            Assertions.assertEquals("a1", updatedEntity.get().getAuthor());
        }

    }

    private void assertEqualsRecommendation(RecommendationEntity expectedEntity, RecommendationEntity actualEntity) {
        Assertions.assertEquals(expectedEntity.getId(), actualEntity.getId());
        Assertions.assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        Assertions.assertEquals(expectedEntity.getProductId(), actualEntity.getProductId());
        Assertions.assertEquals(expectedEntity.getRecommendationId(), actualEntity.getRecommendationId());
        Assertions.assertEquals(expectedEntity.getAuthor(), actualEntity.getAuthor());
        Assertions.assertEquals(expectedEntity.getRating(), actualEntity.getRating());
        Assertions.assertEquals(expectedEntity.getContent(), actualEntity.getContent());
    }
}
