-- Shine All: supplied brand logo and eleven product entries.
-- Run against the clearly_store database after the catalog schema is available.

START TRANSACTION;

UPDATE brands
SET logo_url = 'assets/shine-all-products/ShineAll Home Care Logo.png', enabled = TRUE
WHERE slug = 'shine-all';

INSERT INTO subcategories(category_id,name,slug,description,sort_order,enabled)
SELECT 1,'Kitchen Accessories','kitchen-accessories','Mop sticks, scrub pads and waste-management accessories.',30,TRUE
WHERE NOT EXISTS (SELECT 1 FROM subcategories WHERE category_id=1 AND slug='kitchen-accessories');

INSERT INTO subcategories(category_id,name,slug,description,sort_order,enabled)
SELECT 3,'Restaurant Cleaning','restaurant-cleaning','Cleaning solutions for restaurant utensils and food-service areas.',10,TRUE
WHERE NOT EXISTS (SELECT 1 FROM subcategories WHERE category_id=3 AND slug='restaurant-cleaning');

INSERT INTO brand_categories(brand_id,category_id,enabled)
SELECT b.id,c.id,TRUE FROM brands b JOIN categories c
WHERE b.slug='shine-all' AND c.slug IN ('home-care-cleaning','restaurant-food-service')
ON DUPLICATE KEY UPDATE enabled=TRUE;

-- Refresh the six former placeholder entries with supplied Shine All products.
UPDATE products p
JOIN brands b ON b.id=p.brand_id
JOIN subcategories s ON s.slug='floor-care' AND s.category_id=1
SET p.name='Shine All Lavender Floor Cleaner',p.category_id=1,p.subcategory_id=s.id,
    p.price=199,p.original_price=249,p.image_url='assets/shine-all-products/floor-cleaner-lavender/ShineAll Lavender Floor Cleaner Jug.png',
    p.description='Lavender-fragranced floor cleaner for everyday home and commercial floor care.',p.status='ACTIVE'
WHERE b.slug='shine-all' AND p.product_code='PROD-21';

UPDATE products p
JOIN brands b ON b.id=p.brand_id
JOIN subcategories s ON s.slug='bathroom-care' AND s.category_id=1
SET p.name='Shine All Marine Toilet Cleaner',p.category_id=1,p.subcategory_id=s.id,
    p.price=149,p.original_price=199,p.image_url='assets/shine-all-products/toilet-cleaner/ShineAll Marine Fresh Toilet Cleaner Ad.png',
    p.description='Marine-fresh toilet cleaner for regular bathroom cleaning.',p.status='ACTIVE'
WHERE b.slug='shine-all' AND p.product_code='PROD-22';

UPDATE products p
JOIN brands b ON b.id=p.brand_id
JOIN subcategories s ON s.slug='kitchen-care' AND s.category_id=1
SET p.name='Shine All Multipurpose Cleaner Liquid',p.category_id=1,p.subcategory_id=s.id,
    p.price=179,p.original_price=229,p.image_url='assets/shine-all-products/Multipurpose-cleaner-liquid/ShineAll Multipurpose Cleaner Poster(1).png',
    p.description='Multipurpose cleaning liquid for kitchen and everyday hard surfaces.',p.status='ACTIVE'
WHERE b.slug='shine-all' AND p.product_code='PROD-23';

UPDATE products p
JOIN brands b ON b.id=p.brand_id
JOIN subcategories s ON s.slug='floor-care' AND s.category_id=1
SET p.name='Shine All Lemon Floor Cleaner',p.category_id=1,p.subcategory_id=s.id,
    p.price=199,p.original_price=249,p.image_url='assets/shine-all-products/floor-cleaner-lemon/ShineAll Lemon Fresh Floor Cleaner(2).png',
    p.description='Lemon-fresh floor cleaner for bright, clean floors.',p.status='ACTIVE'
WHERE b.slug='shine-all' AND p.product_code='PROD-24';

UPDATE products p
JOIN brands b ON b.id=p.brand_id
JOIN subcategories s ON s.slug='floor-care' AND s.category_id=1
SET p.name='Shine All Rose Floor Cleaner',p.category_id=1,p.subcategory_id=s.id,
    p.price=199,p.original_price=249,p.image_url='assets/shine-all-products/floor-cleaner-rose/ShineAll Rose Freshness Floor Cleaner.png',
    p.description='Rose-fragranced floor cleaner for daily floor care.',p.status='ACTIVE'
WHERE b.slug='shine-all' AND p.product_code='PROD-25';

UPDATE products p
JOIN brands b ON b.id=p.brand_id
JOIN subcategories s ON s.slug='bathroom-care' AND s.category_id=1
SET p.name='Shine All Lemon Air Freshener',p.category_id=1,p.subcategory_id=s.id,
    p.price=129,p.original_price=169,p.image_url='assets/shine-all-products/air-freshners/Lemon Freshness for Happier Moments.png',
    p.description='Fine-mist lemon air freshener for a clean, welcoming room.',p.status='ACTIVE'
WHERE b.slug='shine-all' AND p.product_code='PROD-26';

-- Add the remaining supplied products. Product codes are stable for store and admin links.
INSERT INTO products(product_code,brand_id,category_id,subcategory_id,name,price,original_price,description,image_url,bulk_discount_enabled,featured,status)
SELECT 'SHA-ACC-407',b.id,1,s.id,'Shine All Eco Garbage Bag Rolls',99,129,'Eco garbage bag rolls for cleaner waste management.','assets/shine-all-products/garbage-bags/ShineAll Eco Garbage Bag Rolls.png',TRUE,FALSE,'ACTIVE'
FROM brands b JOIN subcategories s ON s.slug='kitchen-accessories' AND s.category_id=1 WHERE b.slug='shine-all'
ON DUPLICATE KEY UPDATE name=VALUES(name),price=VALUES(price),original_price=VALUES(original_price),description=VALUES(description),image_url=VALUES(image_url),status='ACTIVE';

INSERT INTO products(product_code,brand_id,category_id,subcategory_id,name,price,original_price,description,image_url,bulk_discount_enabled,featured,status)
SELECT 'SHA-ACC-408',b.id,1,s.id,'Shine All Mop Stick',249,299,'Durable mop stick for brighter homes and multiple floor surfaces.','assets/shine-all-products/mop-sticks/ShineAll Mop Stick for Brighter Homes.png',TRUE,FALSE,'ACTIVE'
FROM brands b JOIN subcategories s ON s.slug='kitchen-accessories' AND s.category_id=1 WHERE b.slug='shine-all'
ON DUPLICATE KEY UPDATE name=VALUES(name),price=VALUES(price),original_price=VALUES(original_price),description=VALUES(description),image_url=VALUES(image_url),status='ACTIVE';

INSERT INTO products(product_code,brand_id,category_id,subcategory_id,name,price,original_price,description,image_url,bulk_discount_enabled,featured,status)
SELECT 'SHA-ACC-409',b.id,1,s.id,'Shine All Green Scrub Pad',49,69,'All-purpose green scrub pad for tough everyday cleaning.','assets/shine-all-products/srubbers-green-all-purpose/ShineAll Durable Scrub Pad Power.png',TRUE,FALSE,'ACTIVE'
FROM brands b JOIN subcategories s ON s.slug='kitchen-accessories' AND s.category_id=1 WHERE b.slug='shine-all'
ON DUPLICATE KEY UPDATE name=VALUES(name),price=VALUES(price),original_price=VALUES(original_price),description=VALUES(description),image_url=VALUES(image_url),status='ACTIVE';

INSERT INTO products(product_code,brand_id,category_id,subcategory_id,name,price,original_price,description,image_url,bulk_discount_enabled,featured,status)
SELECT 'SHA-KC-410',b.id,1,s.id,'Shine All Soap Oil Cleaner',179,229,'Soap-oil cleaner for kitchen grease and multi-surface cleaning.','assets/shine-all-products/soap-oil/ShineAll Soap Oil Cleaner Advertisement.png',TRUE,FALSE,'ACTIVE'
FROM brands b JOIN subcategories s ON s.slug='kitchen-care' AND s.category_id=1 WHERE b.slug='shine-all'
ON DUPLICATE KEY UPDATE name=VALUES(name),price=VALUES(price),original_price=VALUES(original_price),description=VALUES(description),image_url=VALUES(image_url),status='ACTIVE';

INSERT INTO products(product_code,brand_id,category_id,subcategory_id,name,price,original_price,description,image_url,bulk_discount_enabled,featured,status)
SELECT 'SHA-RC-411',b.id,3,s.id,'Shine All Restaurant Utensil Cleaning Liquid',299,349,'Restaurant utensil cleaning liquid for food-service washing and cleaning.','assets/shine-all-products/restaurant-utensil-cleaning-liquid/ShineAll Restaurant Utensil Cleaner Ad.png',TRUE,FALSE,'ACTIVE'
FROM brands b JOIN subcategories s ON s.slug='restaurant-cleaning' AND s.category_id=3 WHERE b.slug='shine-all'
ON DUPLICATE KEY UPDATE name=VALUES(name),price=VALUES(price),original_price=VALUES(original_price),description=VALUES(description),image_url=VALUES(image_url),status='ACTIVE';

-- Keep a single, clear package option for each new or refreshed product.
DELETE pp FROM product_packages pp
JOIN products p ON p.id=pp.product_id
JOIN brands b ON b.id=p.brand_id
WHERE b.slug='shine-all';

INSERT INTO product_packages(product_id,label,quantity,unit_id,package_type,package_code,price,original_price,stock_quantity,is_default,enabled,sort_order)
SELECT p.id,'1 L',1,2,'Bottle',CONCAT(p.product_code,'-1L'),p.price,p.original_price,100,TRUE,TRUE,0
FROM products p JOIN brands b ON b.id=p.brand_id
WHERE b.slug='shine-all' AND p.product_code IN ('PROD-21','PROD-22','PROD-23','PROD-24','PROD-25','PROD-26','SHA-KC-410','SHA-RC-411');

INSERT INTO product_packages(product_id,label,quantity,unit_id,package_type,package_code,price,original_price,stock_quantity,is_default,enabled,sort_order)
SELECT p.id,'17 x 19 (30 pcs per roll)',30,6,'Roll',CONCAT(p.product_code,'-SMALL'),99,129,100,FALSE,TRUE,0
FROM products p WHERE p.product_code='SHA-ACC-407';

INSERT INTO product_packages(product_id,label,quantity,unit_id,package_type,package_code,price,original_price,stock_quantity,is_default,enabled,sort_order)
SELECT p.id,'19 x 21 (30 pcs per roll)',30,6,'Roll',CONCAT(p.product_code,'-MEDIUM'),119,149,100,FALSE,TRUE,1
FROM products p WHERE p.product_code='SHA-ACC-407';

INSERT INTO product_packages(product_id,label,quantity,unit_id,package_type,package_code,price,original_price,stock_quantity,is_default,enabled,sort_order)
SELECT p.id,'24 x 32 (15 pcs per roll)',15,6,'Roll',CONCAT(p.product_code,'-LARGE'),129,159,100,FALSE,TRUE,2
FROM products p WHERE p.product_code='SHA-ACC-407';

INSERT INTO product_packages(product_id,label,quantity,unit_id,package_type,package_code,price,original_price,stock_quantity,is_default,enabled,sort_order)
SELECT p.id,'30 x 37 (15 pcs per roll)',15,6,'Roll',CONCAT(p.product_code,'-XL'),159,199,100,FALSE,TRUE,3
FROM products p WHERE p.product_code='SHA-ACC-407';

INSERT INTO product_packages(product_id,label,quantity,unit_id,package_type,package_code,price,original_price,stock_quantity,is_default,enabled,sort_order)
SELECT p.id,'30 x 45 (10 pcs per roll)',10,6,'Roll',CONCAT(p.product_code,'-JUMBO'),179,229,100,FALSE,TRUE,4
FROM products p WHERE p.product_code='SHA-ACC-407';

INSERT INTO product_packages(product_id,label,quantity,unit_id,package_type,package_code,price,original_price,stock_quantity,is_default,enabled,sort_order)
SELECT p.id,'1 unit',1,6,'Unit',CONCAT(p.product_code,'-UNIT'),p.price,p.original_price,100,TRUE,TRUE,0
FROM products p JOIN brands b ON b.id=p.brand_id
WHERE b.slug='shine-all' AND p.product_code IN ('SHA-ACC-408','SHA-ACC-409');

COMMIT;
