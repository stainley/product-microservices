package com.salapp.review.repositories;

import com.salapp.review.model.ReviewEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PersistenceTests {

    @Autowired
    private ReviewRepository repository;

    private ReviewEntity savedEntity;

    @BeforeEach
    void setupDB() {
        repository.deleteAll();

        ReviewEntity entity = new ReviewEntity(1, 2, "a", "s", "c");
        savedEntity = repository.save(entity);

        assertEqualsReview(entity, savedEntity);
    }

    @Test
    void create() {
        ReviewEntity newEntity = new ReviewEntity(1, 3, "a", "s", "c");
        repository.save(newEntity);

        Optional<ReviewEntity> foundEntity = repository.findById(newEntity.getId());

        if (foundEntity.isPresent()) {
            assertEqualsReview(newEntity, foundEntity.get());
            Assertions.assertEquals(2, repository.count());
        }

    }

    @Test
    void update() {
        savedEntity.setAuthor("a2");
        repository.save(savedEntity);

        Optional<ReviewEntity> foundEntity = repository.findById(savedEntity.getId());

        if (foundEntity.isPresent()) {
            Assertions.assertEquals(1, foundEntity.get().getVersion());
            Assertions.assertEquals("a2", foundEntity.get().getAuthor());
        }
    }

    @Test
    void delete() {
        repository.delete(savedEntity);
        Assertions.assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
    void getByProductId() {
        List<ReviewEntity> entityList = repository.findByProductId(savedEntity.getProductId());

        assertThat(entityList).hasSize(1);
        assertEqualsReview(savedEntity, entityList.get(0));
    }

    @Test
    void duplicateError() {
        ReviewEntity entity = new ReviewEntity(1, 2, "a", "s", "c");
        Exception exception = Assertions.assertThrows(DataIntegrityViolationException.class, () -> repository.save(entity));

        assertThat(exception).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void optimisticLockError() {


        try {
            Optional<ReviewEntity> entity1 = repository.findById(savedEntity.getId());
            Optional<ReviewEntity> entity2 = repository.findById(savedEntity.getId());

            if (entity1.isPresent()) {
                entity1.get().setAuthor("a1");
                repository.save(entity1.get());
            }


            Optional<ReviewEntity> updatedEntity = repository.findById(savedEntity.getId());

            if (entity2.isPresent() && updatedEntity.isPresent()) {
                entity2.get().setAuthor("a2");
                repository.save(entity2.get());

                Assertions.assertEquals(1, updatedEntity.get().getVersion());
                Assertions.assertEquals("a1", updatedEntity.get().getAuthor());
            }

        } catch (OptimisticLockingFailureException e) {
            Assertions.assertNotNull(e);
        }
    }

    private void assertEqualsReview(ReviewEntity expectedEntity, ReviewEntity actualEntity) {
        Assertions.assertEquals(expectedEntity.getId(), actualEntity.getId());
        Assertions.assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        Assertions.assertEquals(expectedEntity.getProductId(), actualEntity.getProductId());
        Assertions.assertEquals(expectedEntity.getReviewId(), actualEntity.getReviewId());
        Assertions.assertEquals(expectedEntity.getAuthor(), actualEntity.getAuthor());
        Assertions.assertEquals(expectedEntity.getSubject(), actualEntity.getSubject());
        Assertions.assertEquals(expectedEntity.getContent(), actualEntity.getContent());
    }
}
