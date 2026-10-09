# SkinSense Product Catalog

The product catalog is stored at:

`src/main/resources/products.json`

It is intentionally separate from Gemini and Java recommendation logic. A future recommendation engine can read this JSON without requiring Java code changes when a new product is added.

## Schema

Each product includes:

- `id`: stable lowercase identifier for the exact product/size.
- `name`, `brand`, `category`, `subtype`: display and matching fields. Subtypes distinguish roles such as `hydrating_serum` and `hydrating_toner` without creating disconnected catalogs.
- `spf`, `paRating`: structured sun-protection metadata when verified; `null`/omitted means the claim still needs verification.
- `price`, `currency`, `priceLastVerified`: observed Bangladesh retail price as a number in BDT, plus the date it was checked.
- `retailer`, `productUrl`, `sourceProductUrl`, `imageUrl`: source retailer and real product/image URLs. `productUrl` is retained for compatibility with the first catalog schema; `sourceProductUrl` makes the verification source explicit.
- `skinTypes`: controlled skin-type vocabulary.
- `concerns`: controlled concern vocabulary matching the assessment form.
- `keyIngredients`: normalized ingredient keywords for later matching.
- `ingredients`: ingredient list when verified.
- `sensitivitySuitability`: non-medical matching hint: `low_risk`, `moderate`, or `potentially_irritating`.
- `fragranceFree`, `crueltyFree`: `true`, `false`, or `null` when not verified.
- `verified`, `imageVerified`, `ingredientsVerified`, `readyForRecommendation`: quality flags for future filtering.
- `verificationNotes`: concise notes about what was verified and what is still incomplete.

## Allowed Values

Categories:

- `cleanser`
- `moisturizer`
- `sunscreen`
- `serum`
- `toner`
- `cleansing_oil`
- `cleansing_balm`
- `micellar_water`
- `spot_treatment`
- `exfoliant`

Skin types:

- `normal`
- `oily`
- `dry`
- `combination`
- `sensitive`

Concerns:

- `acne`
- `acne_scars`
- `hyperpigmentation`
- `dark_spots`
- `redness`
- `large_pores`
- `fine_lines_wrinkles`
- `dull_skin`
- `uneven_skin_tone`

## Current Retailers

- Kirei Bangladesh: `https://kireibd.com/`

The Mall BD has not been added to the catalog yet. Kirei was used for this expansion because its public product pages and product API exposed enough fields to verify product names, prices, source URLs and image URLs consistently.

## User-Facing Product Links

The catalog stores retailer URLs for verification, not as the final user-facing shopping destination. `CatalogProduct#getGoogleSearchUrl()` generates a safe Google search URL from:

`brand + " " + name + " Bangladesh"`

Future UI should use that generated URL for buttons such as `Search Product` or `Find Product`, while retaining `sourceProductUrl` internally for auditing.

## Adding A Product

1. Verify the product page exists on a retailer source.
2. Copy the exact product name, price, product URL and image URL.
3. Use numeric BDT price only, such as `1490` or `1643.40`.
   If a current price cannot be verified, leave it `null`, set `readyForRecommendation` to `false`, and document why. Unknown prices are never optimized as zero-cost products.
4. Set `priceLastVerified` to the date checked.
5. Add only supported `skinTypes` and `concerns`.
6. Add full `ingredients` only if a reliable source lists them.
7. Use `null` for fragrance-free or cruelty-free if not verified.
8. Set `readyForRecommendation` to `true` only when core fields, image and ingredients are verified.
9. Run `mvn test` to validate the catalog.

## Validation

`ProductCatalogValidationTests` checks:

- malformed JSON
- duplicate IDs
- duplicate product URLs
- missing product names, brands, prices, retailers, product URLs and image URLs
- malformed source/product/image URLs
- invalid categories
- invalid skin types
- invalid concerns
- invalid prices
- accidental secret/API key text

The validator also reports warnings for incomplete entries, but incomplete entries should not crash the application.

## Catalog Audit — 2026-10-09

The catalog contains 76 exact product/size variants. No duplicate IDs, retailer URLs, or normalized brand/name/size variants were found, and every entry has a positive verified size. Coverage after this audit is:

- Cleansers: 15 (14 recommendation-ready; 12 with full verified ingredient lists).
- First cleansers: 3 cleansing oils, 1 cleansing balm, and 4 micellar-water size/formula variants. Micellar water was the missing category; 100 ml, 125 ml, and 200 ml options were added so lower total budgets are not forced to use the 400 ml bottle.
- Moisturizers: 12, all recommendation-ready (7 with full verified ingredient lists).
- Sunscreens: 16, all recommendation-ready (8 with full verified ingredient lists). Structured SPF was added to 14; the Anua 50 ml and SKIN1004 15 ml variants remain unstructured pending claim verification. The optimizer already rejects the 15 ml mini and other undersized formats from regular-use sunscreen slots.
- Toners: 4, all recommendation-ready (3 with full verified ingredient lists). Hydrating toner coverage was previously underrepresented.
- Serums: 18, all recommendation-ready (8 with full verified ingredient lists). A hydration-specific serum subtype was previously missing.
- Other useful categories: 2 exfoliants and 1 spot treatment.

Added and integrated in this audit:

- Garnier SkinActive Micellar Cleansing Water for Combination & Oily Skin 400 ml (`micellar_water`).
- CeraVe Hydrating Toner 200 ml (`toner` / `hydrating_toner`).
- The Ordinary Amino Acids + B5 30 ml (`serum` / `hydrating_serum`).
- Garnier Micellar Cleansing Water for Sensitive Skin 100 ml (`micellar_water`).
- Garnier Micellar Cleansing Water Pink 125 ml (`micellar_water`).
- Simple Kind to Skin Micellar Cleansing Water 200 ml (`micellar_water`).

`ProductCategory` is the central taxonomy for ordinary categories and multi-category routine roles. The matcher maps cleansing oil, cleansing balm, and micellar water to the first-cleanse role. Hydrating serums are deliberately excluded from concern-targeted treatment slots. The optimizer can add the verified hydration toner and serum to advanced dry/sensitive/redness routines only when doing so preserves or improves budget fit.

Items still needing verification are retained rather than invented: 29 recommendation-ready products lack a full verified ingredient list, and two sunscreen variants lack structured SPF metadata. The validator reports these as warnings so they remain visible for future catalog maintenance.
