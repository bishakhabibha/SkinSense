# SkinSense Product Catalog

The product catalog is stored at:

`src/main/resources/products.json`

It is intentionally separate from Gemini and Java recommendation logic. A future recommendation engine can read this JSON without requiring Java code changes when a new product is added.

## Schema

Each product includes:

- `id`: stable lowercase identifier for the exact product/size.
- `name`, `brand`, `category`: display and matching fields.
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
