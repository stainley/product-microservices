package com.salapp.product.services;

import com.salapp.api.core.product.Product;
import com.salapp.product.model.ProductEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

public class MapperTests {

    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    public void mapperTest() {

        Assertions.assertNotNull(mapper);

        Product api = new Product(1, "n", 1, "sa");

        ProductEntity entity = mapper.apiToEntity(api);

        Assertions.assertEquals(api.getProductId(), entity.getProductId());
        Assertions.assertEquals(api.getName(), entity.getName());
        Assertions.assertEquals(api.getWeight(), entity.getWeight());

        Product api2 = mapper.entityToApi(entity);

        Assertions.assertEquals(api.getProductId(), api2.getProductId());
        Assertions.assertEquals(api.getName(), api2.getName());
        Assertions.assertEquals(api.getWeight(), api2.getWeight());
        Assertions.assertNull(api2.getServiceAddress());
    }
}
