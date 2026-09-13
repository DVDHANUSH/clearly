package com.clearly.store.auth.services;

import com.clearly.store.auth.repositories.AccountPortalRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AccountPortalService {
    private final AccountPortalRepository repository;
    public AccountPortalService(AccountPortalRepository repository) { this.repository = repository; }

    public Object companies(String identity) { return repository.companies(userId(identity)); }

    @Transactional
    public Object createCompany(String identity, Map<String, Object> value) {
        try {
            long userId = userId(identity), id = repository.createCompany(userId, value);
            repository.addAddress(userId, id, value);
            return repository.company(userId, id);
        } catch (IllegalArgumentException error) { throw badRequest(error); }
    }

    public Object company(String identity, long companyId) {
        try { return repository.company(userId(identity), companyId); }
        catch (Exception error) { throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"); }
    }

    public Object section(String identity, long companyId, String section, String from, String to, int page, int size) {
        try {
            repository.company(userId(identity), companyId);
            int safePage = Math.max(0, page), safeSize = Math.min(50, Math.max(5, size));
            long total = repository.count(companyId, section, from, to);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("rows", repository.rows(companyId, section, from, to, safeSize, safePage * safeSize));
            result.put("total", total); result.put("page", safePage); result.put("size", safeSize);
            result.put("pages", Math.max(1, (total + safeSize - 1) / safeSize));
            return result;
        } catch (IllegalArgumentException error) { throw badRequest(error); }
        catch (Exception error) { throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"); }
    }

    public byte[] document(String identity, long companyId, String section, long rowId) {
        try {
            Map<String, Object> company = repository.company(userId(identity), companyId);
            Map<String, Object> row = repository.documentRow(companyId, section, rowId);
            Map<String, Object> seller = repository.seller();
            if ("invoices".equals(section)) {
                Map<String,Object> buyer = new LinkedHashMap<>(company);
                buyer.put("address", company.get("streetAddress"));
                Map<String,Object> item = new LinkedHashMap<>();
                item.put("description", "Products supplied"); item.put("items", row.get("items")); item.put("unit", "Nos."); item.put("amount", row.get("amount"));
                java.math.BigDecimal amount = new java.math.BigDecimal(String.valueOf(row.get("amount")));
                return InvoicePdfBuilder.build(seller, buyer, String.valueOf(row.get("number")), row.get("date"), java.util.List.of(item), amount, java.math.BigDecimal.ZERO, amount);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document pdf = new Document();
            PdfWriter.getInstance(pdf, out); pdf.open();
            String title = switch (section) { case "orders" -> "ORDER SLIP"; case "shipments" -> "SHIPMENT DOCUMENT"; case "invoices" -> "TAX INVOICE"; default -> "LEDGER STATEMENT"; };
            pdf.add(new Paragraph(title, new Font(Font.HELVETICA, 20, Font.BOLD)));
            pdf.add(new Paragraph(String.valueOf(seller.get("displayName"))));
            pdf.add(new Paragraph(join(seller.get("addressLine1"), seller.get("addressLine2"), seller.get("city"), seller.get("state") + " - " + seller.get("postalCode"))));
            pdf.add(new Paragraph("Phone: " + seller.get("phone") + " | Email: " + seller.get("email")));
            pdf.add(new Paragraph(" "));
            pdf.add(new Paragraph("Customer: " + company.get("name"), new Font(Font.HELVETICA, 12, Font.BOLD)));
            pdf.add(new Paragraph(join(company.get("streetAddress"), company.get("city"), company.get("state") + " - " + company.get("postalCode"), company.get("country"))));
            pdf.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(2); table.setWidthPercentage(100);
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                if ("id".equalsIgnoreCase(entry.getKey())) continue;
                table.addCell(label(entry.getKey())); table.addCell(String.valueOf(entry.getValue()));
            }
            pdf.add(table);
            pdf.add(new Paragraph("Generated securely from your Zarnik account."));
            pdf.close(); return out.toByteArray();
        } catch (IllegalArgumentException error) { throw badRequest(error); }
        catch (Exception error) { throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document could not be generated"); }
    }

    @Transactional
    public Object addAddress(String identity, long companyId, Map<String, Object> value) {
        try { repository.addAddress(userId(identity), companyId, value); return Map.of("success", true); }
        catch (IllegalArgumentException error) { throw badRequest(error); }
    }

    @Transactional
    public Object addContact(String identity, long companyId, Map<String, Object> value) {
        try { repository.addContact(userId(identity), companyId, value); return Map.of("success", true); }
        catch (IllegalArgumentException error) { throw badRequest(error); }
    }

    private long userId(String identity) { return repository.userId(identity); }
    private String join(Object... values) { return java.util.Arrays.stream(values).map(String::valueOf).filter(value -> !value.isBlank() && !"null".equals(value)).collect(java.util.stream.Collectors.joining(", ")); }
    private String label(String value) { String spaced=value.replaceAll("([a-z])([A-Z])", "$1 $2").replace('_',' '); return Character.toUpperCase(spaced.charAt(0))+spaced.substring(1); }
    private ResponseStatusException badRequest(Exception error) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, error.getMessage()); }
}
