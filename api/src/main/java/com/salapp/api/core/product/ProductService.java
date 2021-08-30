package com.salapp.api.core.product;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

public interface ProductService {

    @PostMapping(value = "/product", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Product createProduct(@RequestBody Product body);

    /***
     * curl: $HOST:$PORT/product/1
     * @param productId id
     * @return the product, if found, else null
     */
    @GetMapping(value = "/product/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    Product getProduct(@PathVariable int productId);

    /**
     * curl -X DELETE $HOST:$PORT/product/1
     *
     * @param productId product id
     */
    @DeleteMapping(value = "/product/{productId}")
    void deleteProduct(@PathVariable int productId);
}
