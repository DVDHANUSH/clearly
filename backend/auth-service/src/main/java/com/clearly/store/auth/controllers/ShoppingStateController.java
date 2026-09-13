package com.clearly.store.auth.controllers;

import com.clearly.store.auth.services.ShoppingStateService;
import io.swagger.v3.oas.annotations.Operation;
import java.security.Principal;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shop")
public class ShoppingStateController {
    private final ShoppingStateService service;
    public ShoppingStateController(ShoppingStateService service) { this.service = service; }

    @Operation(summary = "Load the signed-in user's cart and wishlist", tags = {"Shopping State"})
    @GetMapping("/state") public Object state(Principal principal) { return service.state(principal.getName()); }

    @Operation(summary = "Replace the signed-in user's cart", tags = {"Shopping State"})
    @PutMapping("/cart") public Object saveCart(Principal principal, @RequestBody Map<String, Object> payload) { return service.saveCart(principal.getName(), payload); }

    @Operation(summary = "Replace the signed-in user's wishlist", tags = {"Shopping State"})
    @PutMapping("/wishlist") public Object saveWishlist(Principal principal, @RequestBody Map<String, Object> payload) { return service.saveWishlist(principal.getName(), payload); }
}
