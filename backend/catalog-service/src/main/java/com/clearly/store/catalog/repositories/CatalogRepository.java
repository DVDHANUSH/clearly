package com.clearly.store.catalog.repositories;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Set;

@Repository
public class CatalogRepository {
    private final JdbcTemplate jdbc;

    public CatalogRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostConstruct
    public void ensureHsnCodeColumn() {
        Integer count = jdbc.queryForObject("""
            SELECT COUNT(*) FROM information_schema.columns
            WHERE table_schema=DATABASE() AND table_name='products' AND column_name='hsn_code'
            """, Integer.class);
        if (count == null || count == 0) {
            jdbc.execute("ALTER TABLE products ADD COLUMN hsn_code VARCHAR(8) NULL AFTER product_code");
        }
    }

    private static final Map<String,String> HOMEPAGE_DEFAULTS = Map.ofEntries(
            Map.entry("heroEyebrow", "CLEAN HOME. HAPPY LIFE."),
            Map.entry("heroTitleLine1", "Cleaning"),
            Map.entry("heroTitleLine2", "Made Effortless,"),
            Map.entry("heroTitleAccent", "Results That Shine."),
            Map.entry("heroDescription", "Powerful cleaning products for a healthier home.\nTop quality. Trusted by thousands."),
            Map.entry("heroButtonLabel", "Shop Now"),
            Map.entry("heroButtonUrl", "#shop"),
            Map.entry("heroImage", "assets/cleaning-hero.png"),
            Map.entry("heroMobileImage", "assets/cleaning-hero-mobile.png"),
            Map.entry("offerEyebrow", "LIMITED TIME OFFER"),
            Map.entry("offerTitle", "Super Clean"),
            Map.entry("offerAccent", "Super Savings!"),
            Map.entry("offerDescription", "Get up to 25% OFF on best selling products."),
            Map.entry("offerButtonLabel", "Shop Now"),
            Map.entry("offerButtonUrl", "#shop"),
            Map.entry("offerDiscount", "25"),
            Map.entry("promiseEyebrow", "OUR PROMISE"),
            Map.entry("promiseTitle", "A Cleaner Home,"),
            Map.entry("promiseAccent", "A Better Tomorrow."),
            Map.entry("promiseDescription", "We make effective cleaning products that are safe for your family and kind to the planet.")
    );

    private void ensureHomepageTable() {
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS homepage_settings (
              setting_key VARCHAR(80) PRIMARY KEY,
              setting_value TEXT NOT NULL,
              updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """);
    }

    public Map<String,String> homepage() {
        ensureHomepageTable();
        Map<String,String> result = new LinkedHashMap<>(HOMEPAGE_DEFAULTS);
        jdbc.queryForList("SELECT setting_key,setting_value FROM homepage_settings").forEach(row ->
                result.put(String.valueOf(row.get("setting_key")), String.valueOf(row.get("setting_value"))));
        return result;
    }

    public Map<String,String> saveHomepage(Map<String,Object> payload) {
        ensureHomepageTable();
        Set<String> allowed = HOMEPAGE_DEFAULTS.keySet();
        payload.forEach((key,value) -> {
            if (!allowed.contains(key)) return;
            String text = String.valueOf(value == null ? "" : value).trim();
            jdbc.update("""
                INSERT INTO homepage_settings(setting_key,setting_value) VALUES(?,?)
                ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value)
                """, key, text);
        });
        return homepage();
    }

    private String slug(String value) {
        return value.toLowerCase().trim().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }

    private int productId(String value) {
        String sql = value.matches("\\d+") ? "SELECT id FROM products WHERE id=?" : "SELECT id FROM products WHERE product_code=?";
        return ((Number) jdbc.queryForMap(sql, value).get("id")).intValue();
    }

    private Integer lookupId(String sql, Object... args) {
        List<Map<String,Object>> rows = jdbc.queryForList(sql, args);
        return rows.isEmpty() ? null : ((Number) rows.get(0).get("id")).intValue();
    }

    private int brandId(Object value) {
        String key = String.valueOf(value == null ? "" : value).trim();
        Integer id = key.matches("\\d+") ? Integer.valueOf(key)
                : lookupId("SELECT id FROM brands WHERE slug=? OR name=? LIMIT 1", key, key);
        if (id == null) throw new IllegalArgumentException("Unknown brand: " + key);
        return id;
    }

    public List<Map<String,Object>> findAll() {
        List<Map<String,Object>> products = jdbc.queryForList("""
            SELECT p.id, p.product_code AS productCode, p.hsn_code AS hsnCode, p.name,
                   b.id AS brandId, b.slug AS brand, b.name AS brandName,
                   c.id AS categoryId, c.slug AS categorySlug, c.name AS category,
                   s.id AS subcategoryId, s.slug AS subcategorySlug, s.name AS subcategory,
                   p.price, p.original_price AS originalPrice, p.description,
                   p.how_to_use AS howToUse, p.measure_and_dilute AS measureAndDilute,
                   p.image_url AS image, p.document_url AS documentUrl,
                   p.bulk_discount_enabled AS bulkDiscountEnabled,
                   p.featured, p.status, p.created_at AS createdAt, p.updated_at AS updatedAt
            FROM products p
            JOIN brands b ON b.id=p.brand_id
            JOIN categories c ON c.id=p.category_id
            JOIN subcategories s ON s.id=p.subcategory_id
            ORDER BY p.id
            """);
        for (Map<String,Object> product : products) {
            List<Map<String,Object>> packageRows = packages(String.valueOf(product.get("id")));
            product.put("packages", packageRows);
            product.put("sizes", packageRows.stream().filter(row -> Boolean.TRUE.equals(row.get("enabled"))).map(row -> row.get("label")).toList());
            packageRows.stream().filter(row -> Boolean.TRUE.equals(row.get("isDefault"))).findFirst().or(() -> packageRows.stream().findFirst()).ifPresent(row -> {
                product.put("price", row.get("price"));
                product.put("originalPrice", row.get("originalPrice"));
            });
        }
        return products;
    }

    public Map<String,Object> findOne(String id) {
        String where = id.matches("\\d+") ? "p.id=?" : "p.product_code=?";
        String sql = """
            SELECT p.id, p.product_code AS productCode, p.hsn_code AS hsnCode, p.name,
                   b.id AS brandId, b.slug AS brand, b.name AS brandName,
                   c.id AS categoryId, c.slug AS categorySlug, c.name AS category,
                   s.id AS subcategoryId, s.slug AS subcategorySlug, s.name AS subcategory,
                   p.price, p.original_price AS originalPrice, p.description,
                   p.how_to_use AS howToUse, p.measure_and_dilute AS measureAndDilute,
                   p.image_url AS image, p.document_url AS documentUrl,
                   p.bulk_discount_enabled AS bulkDiscountEnabled,
                   p.featured, p.status, p.created_at AS createdAt, p.updated_at AS updatedAt
            FROM products p
            JOIN brands b ON b.id=p.brand_id
            JOIN categories c ON c.id=p.category_id
            JOIN subcategories s ON s.id=p.subcategory_id
            """ + " WHERE " + where;
        List<Map<String,Object>> rows = jdbc.queryForList(sql, id);
        if (rows.isEmpty()) return Map.of("error", "Product not found");
        Map<String,Object> product = new LinkedHashMap<>(rows.get(0));
        int productId = ((Number) product.get("id")).intValue();
        product.put("images", images(String.valueOf(productId)));
        product.put("packages", packages(String.valueOf(productId)));
        product.put("bulkDiscounts", discounts(String.valueOf(productId)));
        product.put("documents", documents(String.valueOf(productId)));
        product.put("specifications", specifications(String.valueOf(productId)));
        return product;
    }

    public List<Map<String,Object>> brands() {
        return jdbc.queryForList("""
            SELECT b.id,b.name,b.slug,b.logo_url AS logoUrl,b.enabled,
                   COUNT(DISTINCT bc.category_id) AS categoryCount,
                   COUNT(DISTINCT p.id) AS productCount,
                   b.created_at AS createdAt,b.updated_at AS updatedAt
            FROM brands b
            LEFT JOIN brand_categories bc ON bc.brand_id=b.id
            LEFT JOIN products p ON p.brand_id=b.id
            GROUP BY b.id ORDER BY b.name
            """);
    }

    public List<Map<String,Object>> categories() {
        List<Map<String,Object>> categories = jdbc.queryForList("""
            SELECT c.id,c.name,c.slug,c.description,c.image_url AS imageUrl,c.sort_order AS sortOrder,c.enabled,
                   COUNT(DISTINCT p.id) AS productCount
            FROM categories c LEFT JOIN products p ON p.category_id=c.id
            GROUP BY c.id ORDER BY c.sort_order,c.name
            """);
        for (Map<String,Object> category : categories) {
            category.put("subcategories", jdbc.queryForList("""
                SELECT s.id,s.name,s.slug,s.description,s.image_url AS imageUrl,s.sort_order AS sortOrder,s.enabled,
                       COUNT(p.id) AS productCount
                FROM subcategories s LEFT JOIN products p ON p.subcategory_id=s.id
                WHERE s.category_id=? GROUP BY s.id ORDER BY s.sort_order,s.name
                """, category.get("id")));
            category.put("brands", jdbc.queryForList("""
                SELECT b.id,b.name,b.slug,b.logo_url AS logoUrl,bc.enabled
                FROM brand_categories bc JOIN brands b ON b.id=bc.brand_id
                WHERE bc.category_id=? ORDER BY b.name
                """, category.get("id")));
        }
        return categories;
    }

    public Map<String,Object> catalog() {
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("products", findAll());
        Map<String,String> brandMap = new LinkedHashMap<>();
        jdbc.queryForList("SELECT slug,name FROM brands WHERE enabled=TRUE ORDER BY name")
                .forEach(row -> brandMap.put(String.valueOf(row.get("slug")), String.valueOf(row.get("name"))));
        List<String> categoryNames = new ArrayList<>();
        jdbc.queryForList("SELECT name FROM categories WHERE enabled=TRUE ORDER BY sort_order,name")
                .forEach(row -> categoryNames.add(String.valueOf(row.get("name"))));
        result.put("brands", brandMap);
        result.put("categories", categoryNames);
        result.put("brandRecords", brands());
        result.put("categoryTree", categories());
        result.put("measurementUnits", units());
        return result;
    }

    public Map<String,Object> saveCatalog(Map<String,Object> payload) {
        Object brandsValue = payload.get("brands");
        if (brandsValue instanceof Map<?,?> entries) {
            entries.forEach((key,value) -> jdbc.update(
                    "INSERT INTO brands(name,slug) VALUES(?,?) ON DUPLICATE KEY UPDATE name=VALUES(name)",
                    String.valueOf(value), String.valueOf(key)));
        }
        return catalog();
    }

    public Map<String,Object> createBrand(Map<String,Object> payload) {
        String name = String.valueOf(payload.getOrDefault("name", "")).trim();
        String key = String.valueOf(payload.getOrDefault("slug", slug(name))).trim();
        jdbc.update("INSERT INTO brands(name,slug,logo_url,enabled) VALUES(?,?,?,?)",
                name, key, payload.get("logoUrl"), payload.getOrDefault("enabled", true));
        return jdbc.queryForMap("SELECT * FROM brands WHERE slug=?", key);
    }

    public Map<String,Object> updateBrand(Integer id, Map<String,Object> payload) {
        jdbc.update("""
            UPDATE brands SET name=COALESCE(?,name),slug=COALESCE(?,slug),
            logo_url=COALESCE(?,logo_url),enabled=COALESCE(?,enabled) WHERE id=?
            """, payload.get("name"), payload.get("slug"), payload.get("logoUrl"), payload.get("enabled"), id);
        return jdbc.queryForMap("SELECT * FROM brands WHERE id=?", id);
    }

    public Map<String,Object> deleteBrand(Integer id) {
        jdbc.update("DELETE FROM brands WHERE id=?", id);
        return Map.of("deleted", id);
    }

    public Map<String,Object> createCategory(Map<String,Object> payload) {
        String name = String.valueOf(payload.getOrDefault("name", "")).trim();
        String key = String.valueOf(payload.getOrDefault("slug", slug(name))).trim();
        jdbc.update("""
            INSERT INTO categories(name,slug,description,image_url,sort_order,enabled)
            VALUES(?,?,?,?,?,?)
            """, name, key, payload.get("description"), payload.get("imageUrl"),
                payload.getOrDefault("sortOrder", 0), payload.getOrDefault("enabled", true));
        return jdbc.queryForMap("SELECT * FROM categories WHERE slug=?", key);
    }

    public Map<String,Object> updateCategory(Integer id, Map<String,Object> payload) {
        jdbc.update("""
            UPDATE categories SET name=COALESCE(?,name),slug=COALESCE(?,slug),
            description=COALESCE(?,description),image_url=COALESCE(?,image_url),
            sort_order=COALESCE(?,sort_order),enabled=COALESCE(?,enabled) WHERE id=?
            """, payload.get("name"), payload.get("slug"), payload.get("description"),
                payload.get("imageUrl"), payload.get("sortOrder"), payload.get("enabled"), id);
        return jdbc.queryForMap("SELECT * FROM categories WHERE id=?", id);
    }

    public Map<String,Object> deleteCategory(Integer id) {
        jdbc.update("DELETE FROM categories WHERE id=?", id);
        return Map.of("deleted", id);
    }

    public List<Map<String,Object>> subcategories(Integer categoryId) {
        return categoryId == null
                ? jdbc.queryForList("SELECT * FROM subcategories ORDER BY category_id,sort_order,name")
                : jdbc.queryForList("SELECT * FROM subcategories WHERE category_id=? ORDER BY sort_order,name", categoryId);
    }

    public Map<String,Object> createSubcategory(Map<String,Object> payload) {
        int categoryId = ((Number) payload.get("categoryId")).intValue();
        String name = String.valueOf(payload.getOrDefault("name", "")).trim();
        String key = String.valueOf(payload.getOrDefault("slug", slug(name))).trim();
        jdbc.update("""
            INSERT INTO subcategories(category_id,name,slug,description,image_url,sort_order,enabled)
            VALUES(?,?,?,?,?,?,?)
            """, categoryId, name, key, payload.get("description"), payload.get("imageUrl"),
                payload.getOrDefault("sortOrder", 0), payload.getOrDefault("enabled", true));
        return jdbc.queryForMap("SELECT * FROM subcategories WHERE category_id=? AND slug=?", categoryId, key);
    }

    public Map<String,Object> updateSubcategory(Integer id, Map<String,Object> payload) {
        jdbc.update("""
            UPDATE subcategories SET name=COALESCE(?,name),slug=COALESCE(?,slug),
            description=COALESCE(?,description),image_url=COALESCE(?,image_url),
            sort_order=COALESCE(?,sort_order),enabled=COALESCE(?,enabled) WHERE id=?
            """, payload.get("name"), payload.get("slug"), payload.get("description"),
                payload.get("imageUrl"), payload.get("sortOrder"), payload.get("enabled"), id);
        return jdbc.queryForMap("SELECT * FROM subcategories WHERE id=?", id);
    }

    public Map<String,Object> deleteSubcategory(Integer id) {
        jdbc.update("DELETE FROM subcategories WHERE id=?", id);
        return Map.of("deleted", id);
    }

    public List<Map<String,Object>> brandCategories(Integer brandId) {
        return jdbc.queryForList("""
            SELECT bc.brand_id AS brandId,bc.category_id AS categoryId,bc.enabled,
                   b.name AS brand,c.name AS category
            FROM brand_categories bc JOIN brands b ON b.id=bc.brand_id
            JOIN categories c ON c.id=bc.category_id
            WHERE (? IS NULL OR bc.brand_id=?) ORDER BY b.name,c.sort_order
            """, brandId, brandId);
    }

    public Map<String,Object> saveBrandCategory(Map<String,Object> payload) {
        int brandId = ((Number) payload.get("brandId")).intValue();
        int categoryId = ((Number) payload.get("categoryId")).intValue();
        jdbc.update("""
            INSERT INTO brand_categories(brand_id,category_id,enabled) VALUES(?,?,?)
            ON DUPLICATE KEY UPDATE enabled=VALUES(enabled)
            """, brandId, categoryId, payload.getOrDefault("enabled", true));
        return jdbc.queryForMap("SELECT * FROM brand_categories WHERE brand_id=? AND category_id=?", brandId, categoryId);
    }

    public Map<String,Object> deleteBrandCategory(Integer brandId, Integer categoryId) {
        jdbc.update("DELETE FROM brand_categories WHERE brand_id=? AND category_id=?", brandId, categoryId);
        return Map.of("deleted", true);
    }

    private int[] resolveTaxonomy(Map<String,Object> payload) {
        Object subValue = payload.get("subcategoryId");
        String categoryValue = String.valueOf(payload.getOrDefault("category", "")).trim();
        String subcategoryValue = String.valueOf(payload.getOrDefault("subcategory", "")).trim();
        Integer subcategoryId = subValue instanceof Number ? ((Number) subValue).intValue() : null;
        if (subcategoryId == null && !subcategoryValue.isBlank()) {
            subcategoryId = lookupId("SELECT id FROM subcategories WHERE slug=? OR name=? LIMIT 1", subcategoryValue, subcategoryValue);
        }
        // Backward compatibility: the old API used category for what is now a subcategory.
        if (subcategoryId == null && !categoryValue.isBlank()) {
            subcategoryId = lookupId("SELECT id FROM subcategories WHERE slug=? OR name=? LIMIT 1", categoryValue, categoryValue);
        }
        if (subcategoryId == null) throw new IllegalArgumentException("subcategoryId or subcategory is required");
        Map<String,Object> row = jdbc.queryForMap("SELECT category_id FROM subcategories WHERE id=?", subcategoryId);
        return new int[]{((Number) row.get("category_id")).intValue(), subcategoryId};
    }

    @Transactional
    public Map<String,Object> saveProduct(Map<String,Object> payload, String id) {
        int brandId = brandId(payload.getOrDefault("brandId", payload.getOrDefault("brand", "")));
        int[] taxonomy = resolveTaxonomy(payload);
        int categoryId = taxonomy[0];
        int subcategoryId = taxonomy[1];
        jdbc.update("""
            INSERT INTO brand_categories(brand_id,category_id,enabled) VALUES(?,?,TRUE)
            ON DUPLICATE KEY UPDATE enabled=TRUE
            """, brandId, categoryId);
        String name = String.valueOf(payload.getOrDefault("name", "Untitled product")).trim();
        String hsnCode = String.valueOf(payload.getOrDefault("hsnCode", "")).trim();
        if (!hsnCode.isBlank() && !hsnCode.matches("\\d{4,8}")) {
            throw new IllegalArgumentException("HSN code must contain 4 to 8 digits");
        }
        BigDecimal price = new BigDecimal(String.valueOf(payload.getOrDefault("price", "0")));
        Object measure = payload.getOrDefault("measureAndDilute", payload.getOrDefault("measureDilute", ""));
        Object howTo = payload.getOrDefault("howToUse", "");
        if (id == null) {
            String code = String.valueOf(payload.getOrDefault("productCode",
                    "PROD-" + UUID.randomUUID().toString().substring(0,8).toUpperCase()));
            jdbc.update("""
                INSERT INTO products(product_code,hsn_code,brand_id,category_id,subcategory_id,name,price,original_price,
                description,how_to_use,measure_and_dilute,image_url,document_url,bulk_discount_enabled,featured,status)
                VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """, code, hsnCode.isBlank() ? null : hsnCode, brandId, categoryId, subcategoryId, name, price, payload.get("originalPrice"),
                    payload.getOrDefault("description", ""), howTo, measure, payload.getOrDefault("image", ""),
                    payload.get("documentUrl"), payload.getOrDefault("bulkDiscountEnabled", false),
                    payload.getOrDefault("featured", false), payload.getOrDefault("status", "ACTIVE"));
            if (payload.containsKey("images")) replaceProductImages(productId(code), payload.get("images"));
            return findOne(code);
        }
        String where = id.matches("\\d+") ? "id=?" : "product_code=?";
        jdbc.update("""
            UPDATE products SET hsn_code=?,brand_id=?,category_id=?,subcategory_id=?,name=?,price=?,original_price=?,
            description=?,how_to_use=?,measure_and_dilute=?,image_url=?,document_url=?,
            bulk_discount_enabled=?,featured=?,status=? WHERE """ + " " + where,
                hsnCode.isBlank() ? null : hsnCode, brandId, categoryId, subcategoryId, name, price, payload.get("originalPrice"),
                payload.getOrDefault("description", ""), howTo, measure, payload.getOrDefault("image", ""),
                payload.get("documentUrl"), payload.getOrDefault("bulkDiscountEnabled", false),
                payload.getOrDefault("featured", false), payload.getOrDefault("status", "ACTIVE"), id);
        if (payload.containsKey("images")) replaceProductImages(productId(id), payload.get("images"));
        return findOne(id);
    }

    private void replaceProductImages(int productId, Object value) {
        if (!(value instanceof List<?> rawImages)) return;
        Set<String> imageUrls = new java.util.LinkedHashSet<>();
        for (Object rawImage : rawImages) {
            if (rawImage == null) continue;
            String imageUrl = String.valueOf(rawImage).trim();
            if (!imageUrl.isBlank()) imageUrls.add(imageUrl);
        }
        jdbc.update("DELETE FROM product_images WHERE product_id=?", productId);
        int sortOrder = 0;
        for (String imageUrl : imageUrls) {
            jdbc.update("INSERT INTO product_images(product_id,image_url,sort_order,alt_text,is_primary) VALUES(?,?,?,?,?)",
                    productId, imageUrl, sortOrder, "", sortOrder == 0);
            sortOrder++;
        }
    }

    public List<Map<String,Object>> images(String id) {
        return jdbc.queryForList("""
            SELECT id,image_url AS imageUrl,sort_order AS sortOrder,alt_text AS altText,
                   is_primary AS isPrimary,created_at AS createdAt
            FROM product_images WHERE product_id=? ORDER BY sort_order,id
            """, productId(id));
    }

    public Map<String,Object> addImage(String id, Map<String,Object> payload) {
        int pid = productId(id);
        int order = payload.get("sortOrder") instanceof Number
                ? ((Number) payload.get("sortOrder")).intValue()
                : jdbc.queryForObject("SELECT COALESCE(MAX(sort_order),-1)+1 FROM product_images WHERE product_id=?", Integer.class, pid);
        jdbc.update("INSERT INTO product_images(product_id,image_url,sort_order,alt_text,is_primary) VALUES(?,?,?,?,?)",
                pid, payload.get("imageUrl"), order, payload.getOrDefault("altText", ""),
                payload.getOrDefault("isPrimary", false));
        return jdbc.queryForMap("SELECT * FROM product_images WHERE id=LAST_INSERT_ID()");
    }

    public Map<String,Object> updateImage(Integer id, Map<String,Object> payload) {
        jdbc.update("""
            UPDATE product_images SET image_url=COALESCE(?,image_url),sort_order=COALESCE(?,sort_order),
            alt_text=COALESCE(?,alt_text),is_primary=COALESCE(?,is_primary) WHERE id=?
            """, payload.get("imageUrl"), payload.get("sortOrder"), payload.get("altText"), payload.get("isPrimary"), id);
        return jdbc.queryForMap("SELECT * FROM product_images WHERE id=?", id);
    }

    public Map<String,Object> deleteImage(Integer id) {
        jdbc.update("DELETE FROM product_images WHERE id=?", id);
        return Map.of("deleted", id);
    }

    public List<Map<String,Object>> units() {
        return jdbc.queryForList("SELECT * FROM measurement_units WHERE enabled=TRUE ORDER BY sort_order,id");
    }

    public List<Map<String,Object>> packages(String id) {
        return jdbc.queryForList("""
            SELECT pp.id,pp.label,pp.quantity,u.code AS unit,u.name AS unitName,u.symbol,
                   pp.package_type AS packageType,pp.package_code AS packageCode,
                   pp.price,pp.original_price AS originalPrice,pp.stock_quantity AS stockQuantity,
                   pp.is_default AS isDefault,pp.enabled,pp.sort_order AS sortOrder
            FROM product_packages pp JOIN measurement_units u ON u.id=pp.unit_id
            WHERE pp.product_id=? ORDER BY pp.sort_order,pp.id
            """, productId(id));
    }

    public Map<String,Object> addPackage(String id, Map<String,Object> payload) {
        int pid = productId(id);
        String unit = String.valueOf(payload.getOrDefault("unit", "UNIT"));
        Integer unitId = lookupId("SELECT id FROM measurement_units WHERE code=? OR symbol=? LIMIT 1",
                unit.toUpperCase(), unit);
        if (unitId == null) throw new IllegalArgumentException("Unknown measurement unit: " + unit);
        boolean first = jdbc.queryForObject("SELECT COUNT(*)=0 FROM product_packages WHERE product_id=?", Boolean.class, pid);
        Object quantity = payload.getOrDefault("quantity", payload.get("sizeValue"));
        String label = String.valueOf(payload.getOrDefault("label", quantity + " " + unit));
        BigDecimal defaultPrice = jdbc.queryForObject("SELECT price FROM products WHERE id=?", BigDecimal.class, pid);
        String packageCode = String.valueOf(payload.getOrDefault("packageCode",
                "PKG-" + UUID.randomUUID().toString().substring(0,8).toUpperCase()));
        jdbc.update("""
            INSERT INTO product_packages(product_id,label,quantity,unit_id,package_type,package_code,price,
            original_price,stock_quantity,is_default,enabled,sort_order)
            VALUES(?,?,?,?,?,?,?,?,?,?,?,?)
            """, pid, label, quantity, unitId, payload.get("packageType"), packageCode,
                payload.getOrDefault("price", defaultPrice), payload.get("originalPrice"),
                payload.getOrDefault("stockQuantity", 0), payload.getOrDefault("isDefault", first),
                payload.getOrDefault("enabled", true), payload.getOrDefault("sortOrder", 0));
        return jdbc.queryForMap("SELECT * FROM product_packages WHERE id=LAST_INSERT_ID()");
    }

    public Map<String,Object> updatePackage(Integer id, Map<String,Object> payload) {
        Integer unitId = null;
        if (payload.get("unit") != null) {
            String unit = String.valueOf(payload.get("unit"));
            unitId = lookupId("SELECT id FROM measurement_units WHERE code=? OR symbol=? LIMIT 1",
                    unit.toUpperCase(), unit);
        }
        jdbc.update("""
            UPDATE product_packages SET label=COALESCE(?,label),quantity=COALESCE(?,quantity),
            unit_id=COALESCE(?,unit_id),package_type=COALESCE(?,package_type),
            package_code=COALESCE(?,package_code),price=COALESCE(?,price),
            original_price=COALESCE(?,original_price),stock_quantity=COALESCE(?,stock_quantity),
            is_default=COALESCE(?,is_default),enabled=COALESCE(?,enabled),
            sort_order=COALESCE(?,sort_order) WHERE id=?
            """, payload.get("label"), payload.getOrDefault("quantity", payload.get("sizeValue")), unitId,
                payload.get("packageType"), payload.get("packageCode"), payload.get("price"),
                payload.get("originalPrice"), payload.get("stockQuantity"), payload.get("isDefault"),
                payload.get("enabled"), payload.get("sortOrder"), id);
        return jdbc.queryForMap("SELECT * FROM product_packages WHERE id=?", id);
    }

    public Map<String,Object> deletePackage(Integer id) {
        jdbc.update("DELETE FROM product_packages WHERE id=?", id);
        return Map.of("deleted", id);
    }

    public List<Map<String,Object>> discounts(String id) {
        return jdbc.queryForList("""
            SELECT d.id,d.package_id AS packageId,pp.label AS packageLabel,
                   d.min_quantity AS minQuantity,d.discount_percent AS discountPercent,
                   d.unit_price AS unitPrice,d.enabled
            FROM bulk_discounts d JOIN product_packages pp ON pp.id=d.package_id
            WHERE pp.product_id=? ORDER BY pp.sort_order,d.min_quantity
            """, productId(id));
    }

    public List<Map<String,Object>> packageDiscounts(Integer packageId) {
        return jdbc.queryForList("""
            SELECT id,package_id AS packageId,min_quantity AS minQuantity,
                   discount_percent AS discountPercent,unit_price AS unitPrice,enabled
            FROM bulk_discounts WHERE package_id=? ORDER BY min_quantity
            """, packageId);
    }

    public Map<String,Object> addDiscount(String product, Map<String,Object> payload) {
        int pid = productId(product);
        Integer packageId = payload.get("packageId") instanceof Number
                ? ((Number) payload.get("packageId")).intValue()
                : lookupId("SELECT id FROM product_packages WHERE product_id=? ORDER BY is_default DESC,sort_order,id LIMIT 1", pid);
        if (packageId == null) throw new IllegalArgumentException("Create a package before adding discounts");
        return addPackageDiscount(packageId, payload);
    }

    public Map<String,Object> addPackageDiscount(Integer packageId, Map<String,Object> payload) {
        BigDecimal packagePrice = jdbc.queryForObject("SELECT price FROM product_packages WHERE id=?", BigDecimal.class, packageId);
        BigDecimal discount = new BigDecimal(String.valueOf(payload.getOrDefault("discountPercent", "0")));
        Object unitPrice = payload.get("unitPrice");
        if (unitPrice == null) unitPrice = packagePrice.multiply(BigDecimal.ONE.subtract(discount.movePointLeft(2)));
        int minimumQuantity = positiveInteger(payload.getOrDefault("minQuantity", 1), "minQuantity");
        jdbc.update("""
            INSERT INTO bulk_discounts(package_id,min_quantity,discount_percent,unit_price,enabled)
            VALUES(?,?,?,?,?)
            """, packageId, minimumQuantity, discount, unitPrice,
                payload.getOrDefault("enabled", true));
        return jdbc.queryForMap("SELECT * FROM bulk_discounts WHERE id=LAST_INSERT_ID()");
    }

    public Map<String,Object> updateDiscount(Integer id, Map<String,Object> payload) {
        Object minimumQuantity = payload.get("minQuantity") == null ? null : positiveInteger(payload.get("minQuantity"), "minQuantity");
        jdbc.update("""
            UPDATE bulk_discounts SET min_quantity=COALESCE(?,min_quantity),
            discount_percent=COALESCE(?,discount_percent),unit_price=COALESCE(?,unit_price),
            enabled=COALESCE(?,enabled) WHERE id=?
            """, minimumQuantity, payload.get("discountPercent"),
                payload.get("unitPrice"), payload.get("enabled"), id);
        return jdbc.queryForMap("SELECT * FROM bulk_discounts WHERE id=?", id);
    }

    public Map<String,Object> deleteDiscount(Integer id) {
        jdbc.update("DELETE FROM bulk_discounts WHERE id=?", id);
        return Map.of("deleted", id);
    }

    private static int positiveInteger(Object value, String field) {
        try {
            BigDecimal number = new BigDecimal(String.valueOf(value));
            if (number.scale() > 0 && number.stripTrailingZeros().scale() > 0) throw new NumberFormatException();
            int result = number.intValueExact();
            if (result < 1) throw new NumberFormatException();
            return result;
        } catch (Exception error) {
            throw new IllegalArgumentException(field + " must be a positive whole number");
        }
    }

    public List<Map<String,Object>> documents(String id) {
        return jdbc.queryForList("""
            SELECT id,document_name AS name,document_url AS url,mime_type AS mimeType,
                   document_type AS documentType,sort_order AS sortOrder,enabled,
                   created_at AS createdAt,updated_at AS updatedAt
            FROM product_documents WHERE product_id=? ORDER BY sort_order,id
            """, productId(id));
    }

    public Map<String,Object> addDocument(String id, Map<String,Object> payload) {
        int pid = productId(id);
        jdbc.update("""
            INSERT INTO product_documents(product_id,document_name,document_url,mime_type,document_type,sort_order,enabled)
            VALUES(?,?,?,?,?,?,?)
            """, pid, payload.getOrDefault("name", "Product document"), payload.get("url"),
                payload.getOrDefault("mimeType", "application/pdf"),
                payload.getOrDefault("documentType", "PRODUCT_DOCUMENT"),
                payload.getOrDefault("sortOrder", 0), payload.getOrDefault("enabled", true));
        return jdbc.queryForMap("SELECT * FROM product_documents WHERE id=LAST_INSERT_ID()");
    }

    public Map<String,Object> updateDocument(Integer id, Map<String,Object> payload) {
        jdbc.update("""
            UPDATE product_documents SET document_name=COALESCE(?,document_name),
            document_url=COALESCE(?,document_url),mime_type=COALESCE(?,mime_type),
            document_type=COALESCE(?,document_type),sort_order=COALESCE(?,sort_order),
            enabled=COALESCE(?,enabled) WHERE id=?
            """, payload.get("name"), payload.get("url"), payload.get("mimeType"),
                payload.get("documentType"), payload.get("sortOrder"), payload.get("enabled"), id);
        return jdbc.queryForMap("SELECT * FROM product_documents WHERE id=?", id);
    }

    public Map<String,Object> deleteDocument(Integer id) {
        jdbc.update("DELETE FROM product_documents WHERE id=?", id);
        return Map.of("deleted", id);
    }

    public List<Map<String,Object>> specifications(String id) {
        return jdbc.queryForList("""
            SELECT id,specification_name AS name,specification_value AS value,sort_order AS sortOrder
            FROM product_specifications WHERE product_id=? ORDER BY sort_order,id
            """, productId(id));
    }

    public Map<String,Object> addSpecification(String id, Map<String,Object> payload) {
        int pid = productId(id);
        jdbc.update("""
            INSERT INTO product_specifications(product_id,specification_name,specification_value,sort_order)
            VALUES(?,?,?,?)
            """, pid, payload.get("name"), payload.get("value"), payload.getOrDefault("sortOrder", 0));
        return jdbc.queryForMap("SELECT * FROM product_specifications WHERE id=LAST_INSERT_ID()");
    }

    public Map<String,Object> updateSpecification(Integer id, Map<String,Object> payload) {
        jdbc.update("""
            UPDATE product_specifications SET specification_name=COALESCE(?,specification_name),
            specification_value=COALESCE(?,specification_value),sort_order=COALESCE(?,sort_order)
            WHERE id=?
            """, payload.get("name"), payload.get("value"), payload.get("sortOrder"), id);
        return jdbc.queryForMap("SELECT * FROM product_specifications WHERE id=?", id);
    }

    public Map<String,Object> deleteSpecification(Integer id) {
        jdbc.update("DELETE FROM product_specifications WHERE id=?", id);
        return Map.of("deleted", id);
    }
}
