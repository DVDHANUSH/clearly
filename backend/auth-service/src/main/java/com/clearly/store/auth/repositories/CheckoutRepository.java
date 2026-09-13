package com.clearly.store.auth.repositories;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class CheckoutRepository {
    private final JdbcTemplate jdbc;
    public CheckoutRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public long userId(String identity) { return jdbc.queryForObject("SELECT id FROM users WHERE email=? OR phone_number=?", Long.class, identity, identity); }
    public Map<String,Object> user(String identity) { return jdbc.queryForMap("SELECT id,full_name AS name,email,phone_number AS phone FROM users WHERE email=? OR phone_number=?", identity, identity); }
    public Map<String,Object> seller() { return jdbc.queryForMap("SELECT id,legal_name AS legalName,display_name AS displayName,phone_number AS phone,email,address_line1 AS addressLine1,address_line2 AS addressLine2,city,state_name AS state,postal_code AS postalCode,country,gstin FROM seller_profiles WHERE active=TRUE ORDER BY id LIMIT 1"); }
    public Map<String,Object> pricedProduct(String ref, String name, String packageCode, String packageSize) {
        List<Map<String,Object>> rows=jdbc.queryForList("""
            SELECT p.product_code AS productRef,p.name,pp.package_code AS packageCode,
                   pp.label AS packageSize,pp.price
            FROM products p JOIN product_packages pp ON pp.product_id=p.id
            WHERE (p.product_code=? OR LOWER(p.name)=LOWER(?)) AND pp.enabled=TRUE
              AND ((?<>'' AND pp.package_code=?) OR (?<>'' AND LOWER(pp.label)=LOWER(?))
                   OR ((?='' AND ?='') AND pp.is_default=TRUE))
            ORDER BY (pp.package_code=?) DESC,(LOWER(pp.label)=LOWER(?)) DESC,
                     pp.is_default DESC,pp.sort_order,pp.id
            LIMIT 1
            """,ref,name,packageCode,packageCode,packageSize,packageSize,packageCode,packageSize,packageCode,packageSize);
        if(rows.isEmpty()) throw new IllegalArgumentException("Product is no longer available: "+name);
        return rows.get(0);
    }
    public long createOrder(long userId,long sellerId,String orderNo,String razorpayOrderId,BigDecimal subtotal,BigDecimal shipping,Map<String,Object> address){
        KeyHolder key=new GeneratedKeyHolder();
        jdbc.update(connection->{PreparedStatement s=connection.prepareStatement("INSERT INTO customer_orders(user_id,seller_id,order_no,razorpay_order_id,subtotal,shipping_amount,total_amount,billing_name,billing_phone,billing_email,billing_address,billing_city,billing_state,billing_postal_code,billing_gstin) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
            s.setLong(1,userId);s.setLong(2,sellerId);s.setString(3,orderNo);s.setString(4,razorpayOrderId);s.setBigDecimal(5,subtotal);s.setBigDecimal(6,shipping);s.setBigDecimal(7,subtotal.add(shipping));s.setString(8,text(address,"name"));s.setString(9,text(address,"phone"));s.setString(10,nullable(address,"email"));s.setString(11,text(address,"address"));s.setString(12,text(address,"city"));s.setString(13,text(address,"state"));s.setString(14,text(address,"postalCode"));s.setString(15,nullable(address,"gstin"));return s;},key);return key.getKey().longValue();
    }
    public void addItem(long orderId,String ref,String name,int quantity,BigDecimal unitPrice){jdbc.update("INSERT INTO customer_order_items(order_id,product_ref,product_name,quantity,unit_price,line_total) VALUES(?,?,?,?,?,?)",orderId,ref,name,quantity,unitPrice,unitPrice.multiply(BigDecimal.valueOf(quantity)));}
    public Map<String,Object> order(long userId,long orderId){return jdbc.queryForMap("SELECT * FROM customer_orders WHERE id=? AND user_id=?",orderId,userId);}
    public List<Map<String,Object>> items(long orderId){return jdbc.queryForList("SELECT product_ref AS productRef,product_name AS productName,quantity,unit_price AS unitPrice,line_total AS lineTotal FROM customer_order_items WHERE order_id=? ORDER BY id",orderId);}
    public void markPaid(long orderId,String paymentId,String providerOrderId,String signature){
        String invoice="INV-"+String.format("%06d",orderId);
        jdbc.update("INSERT INTO payment_transactions(order_id,provider_payment_id,provider_order_id,signature_hash,status) VALUES(?,?,?,?, 'VERIFIED')",orderId,paymentId,providerOrderId,signature);
        jdbc.update("UPDATE customer_orders SET payment_status='PAID',order_status='CONFIRMED',invoice_no=?,paid_at=CURRENT_TIMESTAMP WHERE id=? AND payment_status='PENDING'",invoice,orderId);
        List<Map<String,Object>> companies=jdbc.queryForList("SELECT c.id FROM companies c JOIN customer_orders o ON o.user_id=c.user_id WHERE o.id=? ORDER BY c.created_at,c.id LIMIT 1",orderId);
        if(companies.isEmpty()) return;
        long companyId=((Number)companies.get(0).get("id")).longValue();
        Map<String,Object> order=jdbc.queryForMap("SELECT order_no,total_amount,billing_city,billing_state FROM customer_orders WHERE id=?",orderId);
        Integer itemCount=jdbc.queryForObject("SELECT COALESCE(SUM(quantity),0) FROM customer_order_items WHERE order_id=?",Integer.class,orderId);
        jdbc.update("INSERT IGNORE INTO company_orders(company_id,order_no,ordered_at,items,amount,status) VALUES(?,?,CURRENT_DATE,?,?, 'Confirmed')",companyId,order.get("order_no"),itemCount,order.get("total_amount"));
        jdbc.update("INSERT IGNORE INTO company_invoices(company_id,invoice_no,invoice_date,items,amount,status) VALUES(?,?,CURRENT_DATE,?,?, 'Paid')",companyId,invoice,itemCount,order.get("total_amount"));
        jdbc.update("INSERT IGNORE INTO company_shipments(company_id,shipped_at,tracking_no,carrier,destination,status) VALUES(?,CURRENT_DATE,?,'To be assigned',?,'Preparing')",companyId,"PENDING-"+orderId,order.get("billing_city")+", "+order.get("billing_state"));
        jdbc.update("INSERT INTO company_ledger_entries(company_id,entry_date,document_no,debit,credit,balance) VALUES(?,CURRENT_DATE,?,0,?,0)",companyId,invoice,order.get("total_amount"));
    }
    public void clearCart(long userId){jdbc.update("DELETE FROM user_cart_items WHERE user_id=?",userId);}
    private static String text(Map<String,Object> map,String key){String value=String.valueOf(map.getOrDefault(key,"")).trim();if(value.isBlank())throw new IllegalArgumentException(key+" is required");return value;}
    private static String nullable(Map<String,Object> map,String key){String value=String.valueOf(map.getOrDefault(key,"")).trim();return value.isBlank()?null:value;}
}
