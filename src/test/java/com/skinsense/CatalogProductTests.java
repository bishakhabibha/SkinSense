package com.skinsense;

import com.skinsense.dto.CatalogProduct;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogProductTests {

    @Test
    void googleSearchUrlUsesOnlyBrandAndExactProductName() {
        CatalogProduct product = new CatalogProduct();
        product.setBrand("COSRX");
        product.setName("Low pH Good Morning Gel Cleanser");

        assertThat(product.getGoogleSearchUrl())
                .isEqualTo("https://www.google.com/search?q=COSRX+Low+pH+Good+Morning+Gel+Cleanser");
        assertThat(product.getGoogleSearchUrl()).doesNotContain("Bangladesh");
    }
}
