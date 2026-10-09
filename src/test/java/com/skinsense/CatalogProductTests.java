package com.skinsense;

import com.skinsense.dto.CatalogProduct;
import com.skinsense.dto.ProductMatch;
import com.skinsense.dto.RoutineProductStep;
import com.skinsense.dto.SkincareRoutine;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    @Test
    void variantsWithDifferentIdsRemainDistinctPurchases() {
        SkincareRoutine routine = new SkincareRoutine();
        routine.setMorning(List.of(step("same-product-15ml", "Product", 15)));
        routine.setEvening(List.of(step("same-product-50ml", "Product", 50)));

        assertThat(routine.getUniqueProductCount()).isEqualTo(2);
        assertThat(routine.getUniqueProductSteps())
                .extracting(RoutineProductStep::getProductId)
                .containsExactly("same-product-15ml", "same-product-50ml");
    }

    private RoutineProductStep step(String id, String name, int size) {
        CatalogProduct product = new CatalogProduct();
        product.setId(id);
        product.setName(name);
        product.setSizeValue(java.math.BigDecimal.valueOf(size));
        product.setSizeUnit("ml");
        ProductMatch match = new ProductMatch();
        match.setProduct(product);
        RoutineProductStep step = new RoutineProductStep();
        step.setProductMatch(match);
        return step;
    }
}
