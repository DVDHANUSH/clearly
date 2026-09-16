-- Clearly Store catalog migration 002
-- Adds main categories, subcategories, brand/category availability and package-level commerce data.
USE clearly_store;

ALTER TABLE products DROP FOREIGN KEY fk_products_category;
RENAME TABLE categories TO subcategories;
ALTER TABLE products CHANGE COLUMN category_id subcategory_id INT NULL;

CREATE TABLE categories (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  slug VARCHAR(120) NOT NULL,
  description TEXT NULL,
  image_url VARCHAR(500) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_categories_name UNIQUE (name),
  CONSTRAINT uk_categories_slug UNIQUE (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO categories(name,slug,sort_order) VALUES
  ('Home Care & Cleaning','home-care-cleaning',10),
  ('Restaurant & Food Service','restaurant-food-service',30),
  ('Hotel & Hospitality','hotel-hospitality',40),
  ('Healthcare & Institutions','healthcare-institutions',50),
  ('Laundry Chemicals','laundry-chemicals',70),
  ('Swimming Pool Chemicals','swimming-pool-chemicals',80),
  ('Specialty Chemicals','specialty-chemicals',110),
  ('Construction Chemicals','construction-chemicals',120);

ALTER TABLE subcategories
  ADD COLUMN category_id INT NULL AFTER id,
  ADD COLUMN description TEXT NULL AFTER slug,
  ADD COLUMN image_url VARCHAR(500) NULL AFTER description,
  ADD COLUMN sort_order INT NOT NULL DEFAULT 0 AFTER image_url,
  ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE AFTER sort_order,
  ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER enabled,
  ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

UPDATE subcategories s JOIN categories c ON c.slug='home-care-cleaning'
SET s.category_id=c.id
WHERE s.slug IN ('floor-care','bathroom-care','kitchen-care','laundry-care','surface-care');

UPDATE subcategories s JOIN categories c ON c.slug='construction-chemicals'
SET s.category_id=c.id
WHERE s.slug IN ('tile-care','concrete-repair','waterproofing','precision-grouting','concrete-admixture');

UPDATE subcategories s JOIN categories c ON c.slug='industrial-cleaning'
SET s.category_id=c.id
WHERE s.slug='industrial';

INSERT INTO subcategories(category_id,name,slug,sort_order)
SELECT id,'Glass Care','glass-care',60 FROM categories WHERE slug='home-care-cleaning';
INSERT INTO subcategories(category_id,name,slug,sort_order)
SELECT id,'Furniture Care','furniture-care',70 FROM categories WHERE slug='home-care-cleaning';
INSERT INTO subcategories(category_id,name,slug,sort_order)
SELECT id,'Air Care','air-care',80 FROM categories WHERE slug='home-care-cleaning';

UPDATE products p JOIN brands b ON b.id=p.brand_id JOIN subcategories s ON s.slug='surface-care'
SET p.subcategory_id=s.id WHERE b.slug='diversey' AND p.name='TASKI R2';
UPDATE products p JOIN brands b ON b.id=p.brand_id JOIN subcategories s ON s.slug='glass-care'
SET p.subcategory_id=s.id WHERE b.slug='diversey' AND p.name='TASKI R3';
UPDATE products p JOIN brands b ON b.id=p.brand_id JOIN subcategories s ON s.slug='furniture-care'
SET p.subcategory_id=s.id WHERE b.slug='diversey' AND p.name='TASKI R4 Shine-Up';
UPDATE products p JOIN brands b ON b.id=p.brand_id JOIN subcategories s ON s.slug='air-care'
SET p.subcategory_id=s.id WHERE b.slug='diversey' AND p.name='TASKI R5';
UPDATE products p JOIN brands b ON b.id=p.brand_id JOIN subcategories s ON s.slug='floor-care'
SET p.subcategory_id=s.id WHERE b.slug='diversey' AND p.name='TASKI R7';

ALTER TABLE subcategories
  MODIFY COLUMN category_id INT NOT NULL,
  DROP INDEX name,
  DROP INDEX slug,
  ADD CONSTRAINT uk_subcategories_category_name UNIQUE(category_id,name),
  ADD CONSTRAINT uk_subcategories_category_slug UNIQUE(category_id,slug),
  ADD CONSTRAINT uk_subcategories_id_category UNIQUE(id,category_id),
  ADD CONSTRAINT fk_subcategories_category FOREIGN KEY(category_id) REFERENCES categories(id);

ALTER TABLE products
  ADD COLUMN category_id INT NULL AFTER brand_id,
  ADD COLUMN measure_and_dilute TEXT NULL AFTER how_to_use,
  ADD COLUMN featured BOOLEAN NOT NULL DEFAULT FALSE AFTER bulk_discount_enabled,
  ADD COLUMN status ENUM('DRAFT','ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE' AFTER featured;

UPDATE products p JOIN subcategories s ON s.id=p.subcategory_id
SET p.category_id=s.category_id, p.measure_and_dilute=p.how_to_use;

CREATE TABLE brand_categories (
  brand_id INT NOT NULL,
  category_id INT NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY(brand_id,category_id),
  CONSTRAINT fk_brand_categories_brand FOREIGN KEY(brand_id) REFERENCES brands(id) ON DELETE CASCADE,
  CONSTRAINT fk_brand_categories_category FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO brand_categories(brand_id,category_id)
SELECT DISTINCT brand_id,category_id FROM products;

ALTER TABLE products
  MODIFY COLUMN product_code VARCHAR(80) NOT NULL,
  MODIFY COLUMN category_id INT NOT NULL,
  MODIFY COLUMN subcategory_id INT NOT NULL,
  ADD CONSTRAINT fk_products_brand_category FOREIGN KEY(brand_id,category_id) REFERENCES brand_categories(brand_id,category_id),
  ADD CONSTRAINT fk_products_subcategory_category FOREIGN KEY(subcategory_id,category_id) REFERENCES subcategories(id,category_id);

ALTER TABLE brands
  ADD COLUMN logo_url VARCHAR(500) NULL AFTER slug,
  ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE AFTER logo_url,
  ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER enabled,
  ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

UPDATE brands SET logo_url=CONCAT('assets/brands/',slug,IF(slug='shine-all','.svg','.png'));

CREATE TABLE measurement_units (
  id SMALLINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(20) NOT NULL,
  name VARCHAR(60) NOT NULL,
  symbol VARCHAR(20) NOT NULL,
  dimension ENUM('VOLUME','MASS','COUNT') NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT uk_measurement_units_code UNIQUE(code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO measurement_units(code,name,symbol,dimension,sort_order) VALUES
  ('ML','Millilitre','ml','VOLUME',10),
  ('L','Litre','L','VOLUME',20),
  ('MG','Milligram','mg','MASS',30),
  ('G','Gram','g','MASS',40),
  ('KG','Kilogram','kg','MASS',50),
  ('UNIT','Unit','unit','COUNT',60);

ALTER TABLE product_packages
  CHANGE COLUMN size_value quantity DECIMAL(10,3) NOT NULL,
  ADD COLUMN unit_id SMALLINT NULL AFTER quantity,
  ADD COLUMN package_type VARCHAR(40) NULL AFTER unit_id,
  ADD COLUMN package_code VARCHAR(100) NULL AFTER package_type,
  ADD COLUMN price DECIMAL(10,2) NULL AFTER package_code,
  ADD COLUMN original_price DECIMAL(10,2) NULL AFTER price,
  ADD COLUMN stock_quantity INT NOT NULL DEFAULT 0 AFTER original_price,
  ADD COLUMN is_default BOOLEAN NOT NULL DEFAULT FALSE AFTER stock_quantity,
  ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE AFTER is_default,
  ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER sort_order,
  ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

UPDATE product_packages pp JOIN measurement_units u ON u.code=CASE
  WHEN LOWER(pp.unit)='ml' THEN 'ML'
  WHEN LOWER(pp.unit) IN ('l','l bucket','l barrel') THEN 'L'
  WHEN LOWER(pp.unit)='mg' THEN 'MG'
  WHEN LOWER(pp.unit)='g' THEN 'G'
  WHEN LOWER(pp.unit)='kg' THEN 'KG'
  ELSE 'UNIT' END
SET pp.unit_id=u.id,
    pp.package_type=CASE WHEN LOWER(pp.unit)='l bucket' THEN 'Bucket' WHEN LOWER(pp.unit)='l barrel' THEN 'Barrel' ELSE NULL END;

UPDATE product_packages pp JOIN products p ON p.id=pp.product_id
SET pp.price=p.price, pp.original_price=p.original_price,
    pp.package_code=CONCAT(p.product_code,'-',pp.id);

UPDATE product_packages pp JOIN (
  SELECT product_id,MIN(id) id FROM product_packages GROUP BY product_id
) defaults ON defaults.id=pp.id SET pp.is_default=TRUE;

ALTER TABLE product_packages
  MODIFY COLUMN unit_id SMALLINT NOT NULL,
  MODIFY COLUMN price DECIMAL(10,2) NOT NULL,
  DROP COLUMN unit,
  ADD CONSTRAINT uk_product_packages_code UNIQUE(package_code),
  ADD CONSTRAINT uk_product_packages_label UNIQUE(product_id,label),
  ADD CONSTRAINT fk_packages_unit FOREIGN KEY(unit_id) REFERENCES measurement_units(id);

ALTER TABLE bulk_discounts ADD COLUMN package_id INT NULL AFTER id;
UPDATE bulk_discounts d JOIN product_packages pp ON pp.product_id=d.product_id AND pp.is_default=TRUE
SET d.package_id=pp.id;
ALTER TABLE bulk_discounts
  DROP FOREIGN KEY fk_discounts_product,
  DROP COLUMN product_id,
  MODIFY COLUMN package_id INT NOT NULL,
  ADD CONSTRAINT uk_bulk_discounts_package_quantity UNIQUE(package_id,min_quantity),
  ADD CONSTRAINT fk_discounts_package FOREIGN KEY(package_id) REFERENCES product_packages(id) ON DELETE CASCADE;

ALTER TABLE product_images
  ADD COLUMN is_primary BOOLEAN NOT NULL DEFAULT FALSE AFTER alt_text,
  ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER is_primary,
  ADD CONSTRAINT uk_product_images_order UNIQUE(product_id,sort_order);
UPDATE product_images i JOIN (SELECT product_id,MIN(id) id FROM product_images GROUP BY product_id) p ON p.id=i.id
SET i.is_primary=TRUE;

ALTER TABLE product_documents
  ADD COLUMN document_type VARCHAR(50) NOT NULL DEFAULT 'PRODUCT_DOCUMENT' AFTER mime_type,
  ADD COLUMN sort_order INT NOT NULL DEFAULT 0 AFTER document_type,
  ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE AFTER sort_order,
  ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

CREATE TABLE product_specifications (
  id INT AUTO_INCREMENT PRIMARY KEY,
  product_id INT NOT NULL,
  specification_name VARCHAR(120) NOT NULL,
  specification_value VARCHAR(500) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_product_specification_name UNIQUE(product_id,specification_name),
  CONSTRAINT fk_specifications_product FOREIGN KEY(product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
