package com.salapp.api.core.product;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface ProductService {

    /***
     * curl: $HOST:$PORT/product/1
     * @param productId id
     * @return the product, if found, else null
     */
    @GetMapping(value = "/product/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    Product getProduct(@PathVariable int productId);
}
