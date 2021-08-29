package com.salapp.product.repositories;

import com.salapp.product.model.ProductEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
public class ProductRepositoryTests {

    @Autowired
    private ProductRepository repository;

    private ProductEntity savedEntity;

    @BeforeEach
    void setUpDB() {
        repository.deleteAll();

        ProductEntity entity = new ProductEntity(1, "n", 1);
        savedEntity = repository.save(entity);

        Assertions.assertEquals(entity, savedEntity);
    }

    @Test
    void create() {
        ProductEntity newEntity = new ProductEntity(2, "n", 2);
        repository.save(newEntity);

        ProductEntity foundEntity = repository.findById(newEntity.getId()).get();
        Assertions.assertEquals(newEntity.getId(), foundEntity.getId());

        Assertions.assertEquals(2, repository.count());
    }

    @Test
    void update() {
        savedEntity.setName("n2");
        repository.save(savedEntity);

        ProductEntity foundEntity = repository.findById(savedEntity.getId()).get();
        Assertions.assertEquals(1, foundEntity.getVersion());
        Assertions.assertEquals("n2", foundEntity.getName());
    }

    @Test
    void delete() {
        repository.delete(savedEntity);
        Assertions.assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
    void getByProductId() {
        Optional<ProductEntity> entity = repository.findByProductId(savedEntity.getProductId());

        Assertions.assertTrue(entity.isPresent());
        Assertions.assertEquals(savedEntity, entity.get());
    }

    @Test
    void duplicateError() {
        ProductEntity entity = new ProductEntity(savedEntity.getProductId(), "n", 1);

        Exception exception = Assertions.assertThrows(DuplicateKeyException.class, () -> repository.save(entity));
        assertThat(exception).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void optimisticLockError() {
        ProductEntity entity1 = repository.findById(savedEntity.getId()).get();
        ProductEntity entity2 = repository.findById(savedEntity.getId()).get();

        entity1.setName("n1");
        repository.save(entity1);

        try {
            entity2.setName("n2");
            repository.save(entity2);

            Assertions.fail("Expected an OptimisticLockingFailureException");
        } catch (OptimisticLockingFailureException e) {
        }

        ProductEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        Assertions.assertEquals(1, updatedEntity.getVersion());
        Assertions.assertEquals("n1", updatedEntity.getName());
    }

    @Test
    void paging() {
        repository.deleteAll();

        List<ProductEntity> newProducts = IntStream.rangeClosed(1001, 1010)
                .mapToObj(i -> new ProductEntity(i, "name " + i, i))
                .collect(Collectors.toList());

        repository.saveAll(newProducts);

        Pageable nextPage = PageRequest.of(0, 4, Sort.Direction.ASC, "productId");
        nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true);
        nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true);
        nextPage = testNextPage(nextPage, "[1009, 1010]", false);
    }

    private Pageable testNextPage(Pageable nextPage, String expectedProductIds, boolean expectsNextPage) {
        Page<ProductEntity> productPage = repository.findAll(nextPage);

        Assertions.assertEquals(expectedProductIds, productPage.getContent().stream()
                .map(ProductEntity::getProductId)
                .collect(Collectors.toList()).toString());

        Assertions.assertEquals(expectsNextPage, productPage.hasNext());
        return productPage.nextPageable();
    }
}
