package com.skinsense.util;

import com.skinsense.dto.ProductRecommendation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class ProductImageResolver {

    private static final String PLACEHOLDER_IMAGE = "/images/skincare-placeholder.svg";

    private static final List<ImageRule> IMAGE_RULES = List.of(
            new ImageRule("cerave", "https://www.cerave.com/-/media/project/loreal/brand-sites/cerave/americas/us/skincare/cleansers/hydrating-facial-cleanser/photos/2025/hydrating-facial-cleanser_front.jpg?rev=0dbda3ea882842279d59341505ad4a93"),
            new ImageRule("cetaphil", "https://www.cetaphil.com/dw/image/v2/BGGN_PRD/on/demandware.static/-/Sites-galderma-us-m-catalog/default/dw554054cc/302990100624/GSC-RESIZE-1.2X.png?sw=900&sh=900&sm=fit&q=85"),
            new ImageRule("la roche-posay", "https://media.allure.com/photos/598342d579b7e962c32c43c7/2:3/w_1000,h_1500,c_limit/BOB-2017-Skin-La-Roche-Posay.jpg"),
            new ImageRule("la roche posay", "https://media.allure.com/photos/598342d579b7e962c32c43c7/2:3/w_1000,h_1500,c_limit/BOB-2017-Skin-La-Roche-Posay.jpg"),
            new ImageRule("the ordinary", "https://theordinary.com/dw/image/v2/BFKJ_PRD/on/demandware.static/-/Sites-deciem-master/default/dwce8a7cdf/Images/products/The%20Ordinary/rdn-niacinamide-10pct-zinc-1pct-30ml.png?sw=900&sh=900&sm=fit"),
            new ImageRule("minimalist", "https://global.beminimalist.co/cdn/shop/products/NewNia10_720x_30d37e0e-0147-4caa-8160-449c2bbefbe1.webp?v=1681190554"),
            new ImageRule("bioderma", "https://back-ac-prod.bioderma.com/media/catalog/product/cache/b443460c314aa18d2c50e93a48ddeb50/6/5/652a11af05782f35a3f19d5918a8f7fd-_7b151402_7d__7b_7d__7b28709a_7d.jpg"),
            new ImageRule("paula's choice", "https://www.paulaschoice.com/dw/image/v2/BBNX_PRD/on/demandware.static/-/Sites-pc-catalog/default/dw006e394e/images/products/2-percent-bha-liquid-exfoliant-2010-portrait.png?sw=2000&sfrm=png"),
            new ImageRule("paulas choice", "https://www.paulaschoice.com/dw/image/v2/BBNX_PRD/on/demandware.static/-/Sites-pc-catalog/default/dw006e394e/images/products/2-percent-bha-liquid-exfoliant-2010-portrait.png?sw=2000&sfrm=png"),
            new ImageRule("neutrogena", "https://media.allure.com/photos/57e536e97b47312c0ad83887/2:3/w_1000,h_1500,c_limit/Neutrogena%20Hydro%20Boost%20Water%20Gel%20Jar.jpg"),
            new ImageRule("cosrx", "https://www.cosrx.com/cdn/shop/files/james_800x1067_1_1_4e9750cc-2cd6-4817-ace5-be2305a85806_1200x1200.jpg?v=1763111577"),
            new ImageRule("fixderma", "https://www.fixderma.com/cdn/shop/files/FDSHADOW50PLUSGEL75ML.webp?v=1756357335"),
            new ImageRule("eucerin", "https://media.allure.com/photos/5771940f3b5256713da49e77/2:3/w_1000,h_1500,c_limit/beauty-products-skin-2012-eucerin-daily-protection-face-lotion-spf30.jpg"),
            new ImageRule("aveeno", "https://images.ctfassets.net/j7jv1vcgqjb4/lmohsLgdeVVlZFYuwCayc/041d2b1e39ae8a2c04fc4a9346fd9f5f/Aveeno_the_Power_of_Oat_for_Sensitive_Skin.png?w=1200"),
            new ImageRule("isdin", "https://media.love.isdin.com/webeat/core/icons/isdin-app.png")
    );

    public String resolve(ProductRecommendation product) {
        if (product == null) {
            return PLACEHOLDER_IMAGE;
        }

        String searchableText = ((product.getProductName() == null ? "" : product.getProductName()) + " "
                + (product.getBrand() == null ? "" : product.getBrand()))
                .toLowerCase(Locale.ROOT);

        for (ImageRule rule : IMAGE_RULES) {
            if (searchableText.contains(rule.keyword())) {
                return rule.imageUrl();
            }
        }

        return PLACEHOLDER_IMAGE;
    }

    private record ImageRule(String keyword, String imageUrl) {
    }
}
