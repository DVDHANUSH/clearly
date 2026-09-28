package com.clearly.store.auth.services;

import com.clearly.store.auth.repositories.CheckoutRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CheckoutService {
    private final CheckoutRepository repository; private final ObjectMapper json; private final OrderNotificationClient notifications; private final String keyId; private final String keySecret; private final String storefrontUrl;
    public CheckoutService(CheckoutRepository repository,ObjectMapper json,OrderNotificationClient notifications,@Value("${razorpay.key-id:}") String keyId,@Value("${razorpay.key-secret:}") String keySecret,@Value("${storefront.public-url:http://127.0.0.1:4173}") String storefrontUrl){this.repository=repository;this.json=json;this.notifications=notifications;this.keyId=keyId;this.keySecret=keySecret;this.storefrontUrl=storefrontUrl.replaceAll("/+$","");}
    public Map<String,Object> config(){return Map.of("configured",configured(),"keyId",keyId,"seller",repository.seller());}
    public Map<String,Object> track(String identity,String orderRef){
        String ref=orderRef==null?"":orderRef.trim();if(ref.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Enter your order ID");
        long userId=repository.userId(identity);List<Map<String,Object>> matches=repository.trackedOrder(userId,ref);
        if(matches.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"We could not find this order in your account");
        Map<String,Object> result=new LinkedHashMap<>(matches.get(0));long orderId=((Number)result.get("id")).longValue();result.put("items",repository.items(orderId));return result;
    }

    @Transactional
    public Map<String,Object> create(String identity,Map<String,Object> payload){
        if(!configured())throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Razorpay test keys are not configured on the server");
        try{
            Object raw=payload.get("items");if(!(raw instanceof List<?> requested)||requested.isEmpty())throw new IllegalArgumentException("Your cart is empty");
            List<Map<String,Object>> priced=new ArrayList<>();BigDecimal subtotal=BigDecimal.ZERO;
            for(Object value:requested){if(!(value instanceof Map<?,?> item))continue;String ref=String.valueOf(item.get("productRef"));String name=String.valueOf(item.get("name"));String packageSize=String.valueOf(item.containsKey("packageSize")?item.get("packageSize"):"").trim();String packageCode=String.valueOf(item.containsKey("packageCode")?item.get("packageCode"):"").trim();int quantity=Math.max(1,Math.min(999,Integer.parseInt(String.valueOf(item.get("quantity")))));Map<String,Object> product=repository.pricedProduct(ref,name,packageCode,packageSize);BigDecimal price=new BigDecimal(String.valueOf(product.get("price")));Map<String,Object> row=new LinkedHashMap<>();row.put("productRef",product.get("productRef"));row.put("productName",product.get("name")+" — "+product.get("packageSize"));row.put("quantity",quantity);row.put("unitPrice",price);priced.add(row);subtotal=subtotal.add(price.multiply(BigDecimal.valueOf(quantity)));}
            if(priced.isEmpty())throw new IllegalArgumentException("Your cart is empty");BigDecimal shipping=subtotal.compareTo(BigDecimal.valueOf(499))>=0?BigDecimal.ZERO:BigDecimal.valueOf(59);BigDecimal total=subtotal.add(shipping);String orderNo="CLR-"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))+"-"+java.util.UUID.randomUUID().toString().substring(0,6).toUpperCase();
            String providerId=createRazorpayOrder(total,orderNo);long userId=repository.userId(identity);Map<String,Object> seller=repository.seller();Map<String,Object> address=castMap(payload.get("billingAddress"));long orderId=repository.createOrder(userId,((Number)seller.get("id")).longValue(),orderNo,providerId,subtotal,shipping,address);for(Map<String,Object> row:priced)repository.addItem(orderId,String.valueOf(row.get("productRef")),String.valueOf(row.get("productName")),((Number)row.get("quantity")).intValue(),(BigDecimal)row.get("unitPrice"));
            return Map.of("localOrderId",orderId,"orderNo",orderNo,"razorpayOrderId",providerId,"amount",total.multiply(BigDecimal.valueOf(100)).setScale(0,RoundingMode.HALF_UP).longValueExact(),"currency","INR","keyId",keyId,"seller",seller);
        }catch(ResponseStatusException error){throw error;}catch(Exception error){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,error.getMessage(),error);}
    }
    @Transactional
    public Map<String,Object> verify(String identity,Map<String,Object> payload){
        try{
            long userId=repository.userId(identity),orderId=Long.parseLong(String.valueOf(payload.get("localOrderId")));
            Map<String,Object> order=repository.order(userId,orderId);
            String stored=String.valueOf(order.get("razorpay_order_id")),returned=String.valueOf(payload.get("razorpayOrderId")),payment=String.valueOf(payload.get("razorpayPaymentId")),signature=String.valueOf(payload.get("razorpaySignature"));
            if(!MessageDigest.isEqual(stored.getBytes(StandardCharsets.UTF_8),returned.getBytes(StandardCharsets.UTF_8))||!MessageDigest.isEqual(hmac(stored+"|"+payment).getBytes(StandardCharsets.UTF_8),signature.getBytes(StandardCharsets.UTF_8)))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Payment signature verification failed");
            boolean newlyPaid=repository.markPaid(orderId,payment,stored,signature);
            repository.clearCart(userId);
            if(newlyPaid){
                Map<String,Object> notificationOrder=new LinkedHashMap<>(repository.order(userId,orderId));
                List<Map<String,Object>> notificationItems=repository.items(orderId);
                notificationItems.forEach(this::addEmailProductLinks);
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){
                    @Override public void afterCommit(){notifications.sendPaidOrder(orderId,notificationOrder,notificationItems,payment);}
                });
            }
            return Map.of("success",true,"orderId",orderId,"orderNo",order.get("order_no"),"invoiceUrl","/api/checkout/orders/"+orderId+"/invoice","slipUrl","/api/checkout/orders/"+orderId+"/slip");
        }catch(ResponseStatusException error){throw error;}catch(Exception error){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,error.getMessage(),error);}
    }
    public byte[] document(String identity,long orderId,boolean invoice){long userId=repository.userId(identity);Map<String,Object> order=repository.order(userId,orderId),seller=repository.seller();if(!"PAID".equals(String.valueOf(order.get("payment_status"))))throw new ResponseStatusException(HttpStatus.CONFLICT,"Documents are available only after verified payment");List<Map<String,Object>> items=repository.items(orderId);try{ByteArrayOutputStream out=new ByteArrayOutputStream();Document doc=new Document();PdfWriter.getInstance(doc,out);doc.open();Font title=new Font(Font.HELVETICA,20,Font.BOLD);doc.add(new Paragraph(invoice?"TAX INVOICE":"ORDER SLIP",title));doc.add(new Paragraph(String.valueOf(seller.get("displayName"))));doc.add(new Paragraph(seller.get("addressLine1")+", "+seller.get("addressLine2")));doc.add(new Paragraph(seller.get("city")+", "+seller.get("state")+" - "+seller.get("postalCode")+" | "+seller.get("phone")+" | "+seller.get("email")));doc.add(new Paragraph(" "));doc.add(new Paragraph((invoice?"Invoice: "+order.get("invoice_no"):"Order: "+order.get("order_no"))+"    Date: "+order.get("created_at")));doc.add(new Paragraph("Bill to: "+order.get("billing_name")+", "+order.get("billing_address")+", "+order.get("billing_city")+", "+order.get("billing_state")+" - "+order.get("billing_postal_code")));doc.add(new Paragraph(" "));PdfPTable table=new PdfPTable(4);table.setWidthPercentage(100);for(String heading:List.of("Product","Qty","Unit price","Total"))table.addCell(heading);for(Map<String,Object> row:items){table.addCell(String.valueOf(row.get("productName")));table.addCell(String.valueOf(row.get("quantity")));table.addCell("Rs. "+row.get("unitPrice"));table.addCell("Rs. "+row.get("lineTotal"));}doc.add(table);doc.add(new Paragraph("Subtotal: Rs. "+order.get("subtotal")));doc.add(new Paragraph("Delivery: Rs. "+order.get("shipping_amount")));doc.add(new Paragraph("Grand total: Rs. "+order.get("total_amount"),new Font(Font.HELVETICA,13,Font.BOLD)));doc.add(new Paragraph("Payment status: VERIFIED / PAID"));doc.close();return out.toByteArray();}catch(Exception error){throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Could not generate document",error);}}
    private String createRazorpayOrder(BigDecimal total,String receipt)throws Exception{
        Map<String,Object> body=Map.of("amount",total.multiply(BigDecimal.valueOf(100)).setScale(0,RoundingMode.HALF_UP).longValueExact(),"currency","INR","receipt",receipt);
        String auth=Base64.getEncoder().encodeToString((keyId+":"+keySecret).getBytes(StandardCharsets.UTF_8));
        HttpRequest request=HttpRequest.newBuilder(URI.create("https://api.razorpay.com/v1/orders")).header("Authorization","Basic "+auth).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build();
        HttpResponse<String> response=HttpClient.newHttpClient().send(request,HttpResponse.BodyHandlers.ofString());
        if(response.statusCode()/100!=2){
            if(response.statusCode()==401)throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Razorpay rejected the configured API key and secret. Generate a matching key pair in Razorpay Test Mode and restart the server.");
            String providerMessage="";
            try{providerMessage=json.readTree(response.body()).path("error").path("description").asText("");}catch(Exception ignored){}
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,providerMessage.isBlank()?"Razorpay could not create the payment order":providerMessage);
        }
        JsonNode result=json.readTree(response.body());return result.path("id").asText();
    }
    private String hmac(String value)throws Exception{Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));return java.util.HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));}
    public byte[] brandedInvoice(String identity,long orderId){
        long userId=repository.userId(identity); Map<String,Object> order=repository.order(userId,orderId);
        if(!"PAID".equals(String.valueOf(order.get("payment_status")))) throw new ResponseStatusException(HttpStatus.CONFLICT,"Documents are available only after verified payment");
        Map<String,Object> buyer=new java.util.LinkedHashMap<>(); buyer.put("name",order.get("billing_name")); buyer.put("phone",order.get("billing_phone")); buyer.put("address",order.get("billing_address")); buyer.put("city",order.get("billing_city")); buyer.put("state",order.get("billing_state")); buyer.put("postalCode",order.get("billing_postal_code")); buyer.put("gstin",order.get("billing_gstin"));
        try{return InvoicePdfBuilder.build(repository.seller(),buyer,String.valueOf(order.get("invoice_no")),order.get("created_at"),repository.items(orderId),new BigDecimal(String.valueOf(order.get("subtotal"))),new BigDecimal(String.valueOf(order.get("shipping_amount"))),new BigDecimal(String.valueOf(order.get("total_amount"))));}
        catch(Exception error){throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Could not generate invoice",error);}
    }
    private boolean configured(){return !keyId.isBlank()&&!keySecret.isBlank();}
    private void addEmailProductLinks(Map<String,Object> item){
        String ref=String.valueOf(item.getOrDefault("productRef","")).trim();
        if(!ref.isBlank()) item.put("productUrl",storefrontUrl+"/product.html?id="+java.net.URLEncoder.encode(ref,StandardCharsets.UTF_8));
        String image=String.valueOf(item.getOrDefault("imageUrl","")).trim();
        if(!image.isBlank()&&!image.startsWith("http://")&&!image.startsWith("https://")) item.put("imageUrl",storefrontUrl+"/"+image.replaceFirst("^/+","").replace(" ","%20"));
    }
    @SuppressWarnings("unchecked") private Map<String,Object> castMap(Object value){if(value instanceof Map<?,?> map)return (Map<String,Object>)map;throw new IllegalArgumentException("Billing address is required");}
}
