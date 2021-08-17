package com.salapp.product.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Getter
@Setter
@Document(collection = "products")
@NoArgsConstructor
public class ProductEntity {

    @Id
    private String id;

    @Version
    private Integer version;

    @Indexed(unique = true)
    private int productId;

    private String name;

    private int weight;

    public ProductEntity(int productId, String name, int weight) {
        this.productId = productId;
        this.name = name;
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductEntity)) return false;
        ProductEntity entity = (ProductEntity) o;
        return productId == entity.productId && weight == entity.weight && Objects.equals(id, entity.id) && Objects.equals(version, entity.version) && Objects.equals(name, entity.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version, productId, name, weight);
    }
}
