package com.clearly.store.auth.repositories;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ShoppingStateRepository {
    private final JdbcTemplate jdbc;

    public ShoppingStateRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public long userId(String identity) {
        Long id = jdbc.queryForObject("SELECT id FROM users WHERE email=? OR phone_number=?", Long.class, identity, identity);
        if (id == null) throw new IllegalArgumentException("User not found");
        return id;
    }

    public Map<String, Integer> cart(long userId) {
        Map<String, Integer> result = new LinkedHashMap<>();
        jdbc.queryForList("SELECT product_ref,quantity FROM user_cart_items WHERE user_id=? ORDER BY updated_at,product_ref", userId)
            .forEach(row -> result.put(String.valueOf(row.get("product_ref")), ((Number) row.get("quantity")).intValue()));
        return result;
    }

    public List<String> wishlist(long userId) {
        return jdbc.query("SELECT product_ref FROM user_wishlist_items WHERE user_id=? ORDER BY created_at", (row, index) -> row.getString("product_ref"), userId);
    }

    public void replaceCart(long userId, List<Map<String, Object>> items) {
        jdbc.update("DELETE FROM user_cart_items WHERE user_id=?", userId);
        for (Map<String, Object> item : items) {
            String ref = productRef(item.get("productRef"));
            int quantity = Math.max(1, Integer.parseInt(String.valueOf(item.getOrDefault("quantity", 1))));
            jdbc.update("INSERT INTO user_cart_items(user_id,product_ref,quantity) VALUES(?,?,?)", userId, ref, quantity);
        }
    }

    public void replaceWishlist(long userId, List<String> productRefs) {
        jdbc.update("DELETE FROM user_wishlist_items WHERE user_id=?", userId);
        for (String value : productRefs.stream().distinct().toList()) {
            jdbc.update("INSERT INTO user_wishlist_items(user_id,product_ref) VALUES(?,?)", userId, productRef(value));
        }
    }

    private String productRef(Object value) {
        String ref = String.valueOf(value == null ? "" : value).trim();
        if (ref.isBlank() || ref.length() > 100) throw new IllegalArgumentException("Invalid product reference");
        return ref;
    }
}
