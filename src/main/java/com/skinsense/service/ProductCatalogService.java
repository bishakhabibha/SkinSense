package com.skinsense.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skinsense.dto.ProductCatalog;
import com.skinsense.exception.GeminiException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class ProductCatalogService {

    private final ProductCatalog productCatalog;

    public ProductCatalogService(ObjectMapper objectMapper) {
        this.productCatalog = loadCatalog(objectMapper);
    }

    public ProductCatalog getProductCatalog() {
        return productCatalog;
    }

    private ProductCatalog loadCatalog(ObjectMapper objectMapper) {
        try (InputStream inputStream = new ClassPathResource("products.json").getInputStream()) {
            return objectMapper.readValue(inputStream, ProductCatalog.class);
        } catch (IOException exception) {
            throw new GeminiException("The SkinSense product catalog could not be loaded. Please try again later.", exception);
        }
    }
}
