package com.clearly.store.auth.controllers;

import com.clearly.store.auth.services.AccountPortalService;
import java.security.Principal;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/profile")
public class AccountPortalController {
    private final AccountPortalService service;
    public AccountPortalController(AccountPortalService service) { this.service = service; }

    @GetMapping("/companies") public Object companies(Principal principal) { return service.companies(principal.getName()); }
    @PostMapping("/companies") public Object createCompany(Principal principal, @RequestBody Map<String, Object> value) { return service.createCompany(principal.getName(), value); }
    @GetMapping("/companies/{companyId}") public Object company(Principal principal, @PathVariable long companyId) { return service.company(principal.getName(), companyId); }
    @GetMapping("/companies/{companyId}/{section}")
    public Object section(Principal principal, @PathVariable long companyId, @PathVariable String section,
                          @RequestParam(defaultValue="") String from, @RequestParam(defaultValue="") String to,
                          @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="6") int size) {
        return service.section(principal.getName(), companyId, section, from, to, page, size);
    }
    @GetMapping("/companies/{companyId}/{section}/{rowId}/document")
    public ResponseEntity<byte[]> document(Principal principal, @PathVariable long companyId, @PathVariable String section, @PathVariable long rowId) {
        byte[] body=service.document(principal.getName(),companyId,section,rowId);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+section+"-"+rowId+".pdf\"").body(body);
    }
    @PostMapping("/companies/{companyId}/addresses") public Object addAddress(Principal principal, @PathVariable long companyId, @RequestBody Map<String, Object> value) { return service.addAddress(principal.getName(), companyId, value); }
    @PostMapping("/companies/{companyId}/contacts") public Object addContact(Principal principal, @PathVariable long companyId, @RequestBody Map<String, Object> value) { return service.addContact(principal.getName(), companyId, value); }
}
