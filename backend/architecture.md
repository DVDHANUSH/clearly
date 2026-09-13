# Clearly Store service mapping

The supplied project contains discovery, gateway, config, category, course,
order, notification, enrollment and video services. For this store, the same
service conventions are retained but the domain is reduced to the services we
actually need:

| Reference responsibility | Clearly Store replacement |
|---|---|
| category/course domain | catalog-service |
| order/enrollment domain | order-service |
| notification functions | notification-service |
| discovery-service | discovery-service |
| gateway-service | gateway-service |
| config-server | config-server |
| — | auth-service for Spring Security/JWT |

The catalog model supports:

- products with stable product codes;
- reusable brands, main categories and subcategories;
- many-to-many brand/category availability through `brand_categories`;
- multiple carousel images with explicit display order;
- multiple sellable packages with quantity, measurement unit, package type, price and stock;
- optional bulk-discount tiers owned by an individual package;
- product documents and downloadable files;
- product specifications and buyer-facing use/safety content.

Each product stores one `brand_id`, one `category_id` and one `subcategory_id`.
A composite foreign key guarantees that its subcategory belongs to its selected
main category. A second composite foreign key guarantees that the brand is
enabled for that category. Categories and subcategories therefore remain shared
catalogue data rather than being duplicated for each brand.

`product_packages` is the sellable variant table. A package records quantity,
unit, packaging type, SKU, price, stock and default status. `bulk_discounts`
references a package, allowing a different discount percentage for every size.
Images, documents and specifications remain product-owned child records.
