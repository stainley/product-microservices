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
class ProductRepositoryTests {

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
    void testProductEquals() {
        ProductEntity product1 = new ProductEntity(1, "n", 1);
        ProductEntity product2 = new ProductEntity(1, "n", 1);

        Assertions.assertEquals(product1, product2);
        Assertions.assertEquals(product1.hashCode(), product2.hashCode());
    }

    @Test
    void create() {
        ProductEntity newEntity = new ProductEntity(2, "n", 2);
        repository.save(newEntity);

        Optional<ProductEntity> foundEntity = repository.findById(newEntity.getId());

        foundEntity.ifPresent(productEntity -> {
            Assertions.assertEquals(newEntity.getId(), productEntity.getId());
            Assertions.assertEquals(2, repository.count());
        });
    }

    @Test
    void update() {
        savedEntity.setName("n2");
        repository.save(savedEntity);

        Optional<ProductEntity> foundEntity = repository.findById(savedEntity.getId());

        foundEntity.ifPresent(productEntity -> {
            Assertions.assertEquals(1, productEntity.getVersion());
            Assertions.assertEquals("n2", productEntity.getName());
        });

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
        Optional<ProductEntity> entity1 = repository.findById(savedEntity.getId());
        Optional<ProductEntity> entity2 = repository.findById(savedEntity.getId());

        entity1.ifPresent(productEntity -> {
            productEntity.setName("n1");
            repository.save(productEntity);
        });

        try {
            entity2.ifPresent(productEntity -> {
                productEntity.setName("n2");
                repository.save(productEntity);
            });

            Assertions.fail("Expected an OptimisticLockingFailureException");
        } catch (OptimisticLockingFailureException e) {
            Assertions.assertNotNull(e);
        }

        Optional<ProductEntity> updatedEntity = repository.findById(savedEntity.getId());
        updatedEntity.ifPresent(productEntity -> {
            Assertions.assertEquals(1, productEntity.getVersion());
            Assertions.assertEquals("n1", productEntity.getName());
        });
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

        Assertions.assertNotNull(nextPage);
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
