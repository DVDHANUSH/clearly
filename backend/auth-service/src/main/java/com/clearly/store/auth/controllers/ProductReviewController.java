package com.clearly.store.auth.controllers;

import com.clearly.store.auth.repositories.ProductReviewRepository;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController @RequestMapping("/api/reviews")
public class ProductReviewController {
    private final ProductReviewRepository reviews;
    public ProductReviewController(ProductReviewRepository reviews){this.reviews=reviews;}

    @GetMapping("/product/{productRef}") public List<Map<String,Object>> product(@PathVariable String productRef){return reviews.publicReviews(productRef);}
    @GetMapping("/product/{productRef}/eligibility") public Map<String,Object> eligibility(Principal principal,@PathVariable String productRef){
        long userId=reviews.userId(principal.getName());boolean eligible=reviews.purchased(userId,productRef);
        return Map.of("eligible",eligible,"message",eligible?"You can review this product":"Reviews are available only after purchasing this product");
    }
    @PostMapping("/product/{productRef}") public Map<String,Object> create(Principal principal,@PathVariable String productRef,@RequestBody ReviewRequest body){
        validate(body);long userId=reviews.userId(principal.getName());
        if(!reviews.purchased(userId,productRef))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only customers who purchased this product can review it");
        validateImage(body.imageUrl());reviews.createCustomer(userId,productRef,reviews.userName(userId),body.rating(),body.title(),body.reviewText(),body.imageUrl());
        return Map.of("success",true);
    }
    @PreAuthorize("hasRole('ADMIN')") @GetMapping("/admin") public List<Map<String,Object>> all(){return reviews.allReviews();}
    @PreAuthorize("hasRole('ADMIN')") @PostMapping("/admin") public Map<String,Object> adminCreate(@RequestBody AdminReviewRequest body){
        validate(new ReviewRequest(body.rating(),body.title(),body.reviewText(),body.imageUrl()));
        if(body.productRef()==null||body.productRef().isBlank()||body.reviewerName()==null||body.reviewerName().isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Product and reviewer name are required");
        validateImage(body.imageUrl());reviews.createAdmin(body.productRef().trim(),body.reviewerName().trim(),body.rating(),body.title(),body.reviewText().trim(),body.imageUrl(),body.verifiedPurchase());return Map.of("success",true);
    }
    @PreAuthorize("hasRole('ADMIN')") @DeleteMapping("/admin/{id}") public Map<String,Object> delete(@PathVariable long id){reviews.delete(id);return Map.of("success",true);}
    private void validate(ReviewRequest body){if(body.rating()<1||body.rating()>5)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose a rating from 1 to 5");if(body.reviewText()==null||body.reviewText().trim().length()<5||body.reviewText().length()>1200)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Review must contain 5 to 1200 characters");}
    private void validateImage(String imageUrl){if(imageUrl!=null&&!imageUrl.isBlank()&&(imageUrl.length()>500||!imageUrl.matches("^(https?://(localhost|127\\.0\\.0\\.1):8081)?/api/uploads/[A-Za-z0-9._-]+$")))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid review image");}
    public record ReviewRequest(int rating,String title,String reviewText,String imageUrl){}
    public record AdminReviewRequest(String productRef,String reviewerName,int rating,String title,String reviewText,String imageUrl,boolean verifiedPurchase){}
}
