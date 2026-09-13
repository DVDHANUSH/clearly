package com.order.service.controllers;
import com.order.service.OrderDetail;
import com.order.service.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("api/v1/orders")
public class OrderController {
    private OrderService orderService;
    @Autowired
    private StreamBridge streamBridge;
    public OrderController(OrderService orderService){
        this.orderService = orderService;
    }
    @PostMapping
    public ResponseEntity<?> createOrder(){

        OrderDetail order = this.orderService.createOrder();
        // send notification to notification service, and notification service will send email and message to the server
        orderCreatedNotification(order);
        return ResponseEntity.ok("Order Confirmed");
    }
    private void orderCreatedNotification(OrderDetail orderDetail) {
        // logic to send notification service
       boolean send = streamBridge.send("orderCreatedEvent-out-0", orderDetail);
       if(send){
           System.out.println("Event has been successfully sent");
       }
    }
}