-- Bulk discounts are represented as whole percentages across the admin panel and storefront.
USE clearly_store;

UPDATE bulk_discounts d
JOIN product_packages pp ON pp.id = d.package_id
SET d.discount_percent = ROUND(LEAST(100, GREATEST(0, d.discount_percent))),
    d.unit_price = ROUND(pp.price * (1 - ROUND(LEAST(100, GREATEST(0, d.discount_percent))) / 100), 2);
