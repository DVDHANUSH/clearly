package com.clearly.store.auth.controllers;

import com.clearly.store.auth.services.CheckoutService;
import java.security.Principal;
import java.util.Map;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController @RequestMapping("/api/checkout")
public class CheckoutController {
    private final CheckoutService service; public CheckoutController(CheckoutService service){this.service=service;}
    @GetMapping("/config") public Object config(){return service.config();}
    @PostMapping("/orders") public Object create(Principal principal,@RequestBody Map<String,Object> body){return service.create(principal.getName(),body);}
    @PostMapping("/verify") public Object verify(Principal principal,@RequestBody Map<String,Object> body){return service.verify(principal.getName(),body);}
    @GetMapping("/track") public Object track(Principal principal,@RequestParam String orderId){return service.track(principal.getName(),orderId);}
    @GetMapping("/orders/{id}/invoice") public ResponseEntity<byte[]> invoice(Principal principal,@PathVariable long id){return pdf(service.brandedInvoice(principal.getName(),id),"invoice-"+id+".pdf");}
    @GetMapping("/orders/{id}/slip") public ResponseEntity<byte[]> slip(Principal principal,@PathVariable long id){return pdf(service.document(principal.getName(),id,false),"order-slip-"+id+".pdf");}
    private ResponseEntity<byte[]> pdf(byte[] content,String name){HttpHeaders headers=new HttpHeaders();headers.setContentType(MediaType.APPLICATION_PDF);headers.setContentDisposition(ContentDisposition.attachment().filename(name).build());return ResponseEntity.ok().headers(headers).body(content);}
}
