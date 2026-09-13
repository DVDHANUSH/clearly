package com.clearly.store.auth.services;

import com.clearly.store.auth.repositories.ShoppingStateRepository;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ShoppingStateService {
    private final ShoppingStateRepository repository;
    public ShoppingStateService(ShoppingStateRepository repository) { this.repository = repository; }

    public Object state(String identity) {
        long userId = repository.userId(identity);
        return Map.of("cart", repository.cart(userId), "wishlist", repository.wishlist(userId));
    }

    @Transactional
    public Object saveCart(String identity, Map<String, Object> payload) {
        try {
            long userId = repository.userId(identity);
            Object raw = payload.get("items");
            List<Map<String, Object>> items = raw instanceof List<?> list ? list.stream().filter(Map.class::isInstance).map(value -> (Map<String, Object>) value).toList() : List.of();
            repository.replaceCart(userId, items);
            return Map.of("cart", repository.cart(userId));
        } catch (RuntimeException error) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, error.getMessage()); }
    }

    @Transactional
    public Object saveWishlist(String identity, Map<String, Object> payload) {
        try {
            long userId = repository.userId(identity);
            Object raw = payload.get("productRefs");
            List<String> refs = raw instanceof List<?> list ? list.stream().map(String::valueOf).toList() : List.of();
            repository.replaceWishlist(userId, refs);
            return Map.of("wishlist", repository.wishlist(userId));
        } catch (RuntimeException error) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, error.getMessage()); }
    }
}
