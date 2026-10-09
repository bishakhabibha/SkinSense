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
        assertThat(catalog.getProducts()).hasSize(76);
        assertThat(catalog.getAllowedCategories()).contains("micellar_water", "toner", "serum");
        assertThat(catalog.getProducts()).filteredOn(product ->
                        java.util.Set.of("garnier-skinactive-micellar-water-combination-oily-400ml",
                                        "cerave-hydrating-toner-200ml",
                                        "the-ordinary-amino-acids-b5-30ml",
                                        "garnier-micellar-cleansing-water-sensitive-pink-125ml",
                                        "garnier-micellar-cleansing-water-sensitive-100ml",
                                        "simple-kind-to-skin-micellar-cleansing-water-200ml")
                                .contains(product.getId()))
                .allSatisfy(product -> {
                    assertThat(product.isReadyForRecommendation()).isTrue();
                    assertThat(product.isIngredientsVerified()).isTrue();
                    assertThat(product.getSizeValue()).isPositive();
                    assertThat(product.getPrice()).isPositive();
                    assertThat(product.getSourceProductUrl()).startsWith("https://");
                });
        assertThat(catalog.getProducts())
                .filteredOn(product -> "micellar_water".equals(product.getCategory()))
                .hasSize(4)
                .extracting(product -> product.getSizeValue().intValueExact())
                .containsExactlyInAnyOrder(100, 125, 200, 400);
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
