package com.clearly.store.auth.repositories;

import java.util.List;
import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProductReviewRepository {
    private final JdbcTemplate jdbc;
    public ProductReviewRepository(JdbcTemplate jdbc){this.jdbc=jdbc;}

    public List<Map<String,Object>> publicReviews(String productRef){
        return jdbc.queryForList("""
            SELECT id,product_ref AS productRef,reviewer_name AS reviewerName,rating,title,
                   review_text AS reviewText,image_url AS imageUrl,verified_purchase AS verifiedPurchase,
                   created_by_admin AS createdByAdmin,created_at AS createdAt
            FROM product_reviews WHERE product_ref=? AND enabled=TRUE
            ORDER BY created_at DESC,id DESC
            """,productRef);
    }
    public List<Map<String,Object>> allReviews(){
        return jdbc.queryForList("""
            SELECT id,product_ref AS productRef,reviewer_name AS reviewerName,rating,title,
                   review_text AS reviewText,image_url AS imageUrl,verified_purchase AS verifiedPurchase,
                   created_by_admin AS createdByAdmin,enabled,created_at AS createdAt
            FROM product_reviews ORDER BY created_at DESC,id DESC
            """);
    }
    public long userId(String identity){return jdbc.queryForObject("SELECT id FROM users WHERE email=? OR phone_number=?",Long.class,identity,identity);}
    public String userName(long userId){return jdbc.queryForObject("SELECT full_name FROM users WHERE id=?",String.class,userId);}
    public boolean purchased(long userId,String productRef){
        Integer count=jdbc.queryForObject("""
            SELECT COUNT(*) FROM customer_orders o
            JOIN customer_order_items i ON i.order_id=o.id
            WHERE o.user_id=? AND o.payment_status='PAID' AND i.product_ref=?
            """,Integer.class,userId,productRef);
        return count!=null&&count>0;
    }
    public void createCustomer(long userId,String productRef,String name,int rating,String title,String text,String imageUrl){
        try{jdbc.update("INSERT INTO product_reviews(product_ref,user_id,reviewer_name,rating,title,review_text,image_url,verified_purchase) VALUES(?,?,?,?,?,?,?,TRUE)",productRef,userId,name,rating,blankToNull(title),text,blankToNull(imageUrl));}
        catch(DuplicateKeyException error){throw new IllegalArgumentException("You have already reviewed this product");}
    }
    public void createAdmin(String productRef,String name,int rating,String title,String text,String imageUrl,boolean verified){
        jdbc.update("INSERT INTO product_reviews(product_ref,reviewer_name,rating,title,review_text,image_url,verified_purchase,created_by_admin) VALUES(?,?,?,?,?,?,?,TRUE)",productRef,name,rating,blankToNull(title),text,blankToNull(imageUrl),verified);
    }
    public void delete(long id){jdbc.update("DELETE FROM product_reviews WHERE id=?",id);}
    private String blankToNull(String value){return value==null||value.isBlank()?null:value.trim();}
}
