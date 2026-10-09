package com.skinsense;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skinsense.dto.ProductCatalog;
import com.skinsense.util.ProductCatalogValidator;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

class ProductCatalogValidationTests {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void productCatalogHasValidStructure() throws Exception {
        ProductCatalog catalog = loadCatalog();

        ProductCatalogValidator.ValidationResult result = ProductCatalogValidator.validate(catalog);

        assertThat(result.errors()).isEmpty();
        assertThat(catalog.getProducts()).hasSizeGreaterThanOrEqualTo(10);
    }

    @Test
    void productCatalogDoesNotContainSecrets() throws Exception {
        String rawCatalog;
        try (InputStream inputStream = new ClassPathResource("products.json").getInputStream()) {
            rawCatalog = new String(inputStream.readAllBytes());
        }

        assertThat(rawCatalog).doesNotContain("GEMINI_API_KEY");
        assertThat(rawCatalog).doesNotContainIgnoringCase("api_key");
        assertThat(rawCatalog).doesNotContainIgnoringCase("gemini");
    }

    private ProductCatalog loadCatalog() throws Exception {
        try (InputStream inputStream = new ClassPathResource("products.json").getInputStream()) {
            return objectMapper.readValue(inputStream, ProductCatalog.class);
        }
    }
}
