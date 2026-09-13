package com.clearly.store.auth.repositories;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class AccountPortalRepository {
    private final JdbcTemplate jdbc;

    public AccountPortalRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public long userId(String identity) {
        Long id = jdbc.queryForObject("SELECT id FROM users WHERE email = ? OR phone_number = ?", Long.class, identity, identity);
        if (id == null) throw new IllegalArgumentException("User not found");
        return id;
    }

    public List<Map<String, Object>> companies(long userId) {
        return jdbc.queryForList("""
            SELECT c.id, c.name, c.country, c.city, c.state_name AS state, c.postal_code AS postalCode, c.gstin,
                   (SELECT COUNT(*) FROM company_orders o WHERE o.company_id=c.id) AS orderCount,
                   (SELECT COALESCE(SUM(o.amount),0) FROM company_orders o WHERE o.company_id=c.id) AS orderValue
            FROM companies c WHERE c.user_id=? ORDER BY c.created_at, c.id
            """, userId);
    }

    public long createCompany(long userId, Map<String, Object> value) {
        KeyHolder key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO companies (user_id,name,country,street_address,state_name,city,postal_code,gstin) VALUES (?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, userId); statement.setString(2, text(value, "name")); statement.setString(3, optional(value, "country", "India"));
            statement.setString(4, text(value, "streetAddress")); statement.setString(5, text(value, "state")); statement.setString(6, text(value, "city"));
            statement.setString(7, text(value, "postalCode")); statement.setString(8, nullable(value, "gstin"));
            return statement;
        }, key);
        return key.getKey().longValue();
    }

    public Map<String, Object> company(long userId, long companyId) {
        return jdbc.queryForMap("SELECT id,name,country,street_address AS streetAddress,state_name AS state,city,postal_code AS postalCode,gstin FROM companies WHERE id=? AND user_id=?", companyId, userId);
    }

    public long count(long companyId, String section, String from, String to) {
        Table table = table(section);
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table.name + " WHERE company_id=?" + dateClause(table.dateColumn, from, to), Long.class, params(companyId, from, to));
    }

    public List<Map<String, Object>> rows(long companyId, String section, String from, String to, int limit, int offset) {
        Table table = table(section);
        String sql = "SELECT " + table.columns + " FROM " + table.name + " WHERE company_id=?" + dateClause(table.dateColumn, from, to) + " ORDER BY " + table.orderBy + " LIMIT ? OFFSET ?";
        Object[] base = params(companyId, from, to), all = new Object[base.length + 2];
        System.arraycopy(base, 0, all, 0, base.length); all[base.length] = limit; all[base.length + 1] = offset;
        return jdbc.queryForList(sql, all);
    }

    public Map<String, Object> documentRow(long companyId, String section, long rowId) {
        Table table = table(section);
        if (!List.of("orders", "shipments", "invoices", "ledger").contains(section)) throw new IllegalArgumentException("Documents are not available for this section");
        return jdbc.queryForMap("SELECT " + table.columns + " FROM " + table.name + " WHERE company_id=? AND id=?", companyId, rowId);
    }

    public Map<String, Object> seller() {
        return jdbc.queryForMap("SELECT display_name AS displayName,phone_number AS phone,email,address_line1 AS addressLine1,address_line2 AS addressLine2,city,state_name AS state,postal_code AS postalCode FROM seller_profiles WHERE active=TRUE ORDER BY id LIMIT 1");
    }

    public void addAddress(long userId, long companyId, Map<String, Object> value) {
        company(userId, companyId);
        jdbc.update("INSERT INTO company_addresses (company_id,address_type,street_address,city,state_name,postal_code,country) VALUES (?,?,?,?,?,?,?)",
            companyId, optional(value, "addressType", "Shipping"), text(value, "streetAddress"), text(value, "city"), text(value, "state"), text(value, "postalCode"), optional(value, "country", "India"));
    }

    public void addContact(long userId, long companyId, Map<String, Object> value) {
        company(userId, companyId);
        jdbc.update("INSERT INTO company_contacts (company_id,first_name,last_name,email,phone_number,contact_role,phone_verified) VALUES (?,?,?,?,?,?,FALSE)",
            companyId, text(value, "firstName"), nullable(value, "lastName"), nullable(value, "email"), text(value, "phone"), nullable(value, "role"));
    }

    private Table table(String section) {
        return switch (section) {
            case "orders" -> new Table("company_orders", "ordered_at", "id,ordered_at AS date, order_no AS number, items, amount, status", "ordered_at DESC,id DESC");
            case "shipments" -> new Table("company_shipments", "shipped_at", "id,shipped_at AS date, tracking_no AS tracking, carrier, destination, status", "shipped_at DESC,id DESC");
            case "invoices" -> new Table("company_invoices", "invoice_date", "id,invoice_date AS date, invoice_no AS number, items, amount, status", "invoice_date DESC,id DESC");
            case "ledger" -> new Table("company_ledger_entries", "entry_date", "id,entry_date AS date, document_no AS document, debit, credit, balance", "entry_date DESC,id DESC");
            case "payments" -> new Table("(SELECT * FROM company_ledger_entries WHERE credit > 0) company_ledger_entries", "entry_date", "entry_date AS date, document_no AS reference, credit AS amount, 'Received' AS status", "entry_date DESC,id DESC");
            case "addresses" -> new Table("company_addresses", null, "id,address_type AS type,street_address AS address,city,state_name AS state,postal_code AS pincode,country", "id DESC");
            case "contacts" -> new Table("company_contacts", null, "id,CONCAT(first_name,' ',COALESCE(last_name,'')) AS name,email,phone_number AS phone,contact_role AS role,phone_verified AS phoneVerified", "id DESC");
            default -> throw new IllegalArgumentException("Unknown company section");
        };
    }

    private String dateClause(String column, String from, String to) {
        if (column == null) return "";
        String sql = "";
        if (from != null && !from.isBlank()) sql += " AND " + column + ">=?";
        if (to != null && !to.isBlank()) sql += " AND " + column + "<=?";
        return sql;
    }

    private Object[] params(long companyId, String from, String to) {
        java.util.ArrayList<Object> values = new java.util.ArrayList<>(); values.add(companyId);
        if (from != null && !from.isBlank()) values.add(from);
        if (to != null && !to.isBlank()) values.add(to);
        return values.toArray();
    }

    private static String text(Map<String, Object> value, String key) {
        String result = String.valueOf(value.getOrDefault(key, "")).trim();
        if (result.isBlank()) throw new IllegalArgumentException(key + " is required");
        return result;
    }
    private static String nullable(Map<String, Object> value, String key) { String result = String.valueOf(value.getOrDefault(key, "")).trim(); return result.isBlank() ? null : result; }
    private static String optional(Map<String, Object> value, String key, String fallback) { String result = nullable(value, key); return result == null ? fallback : result; }
    private record Table(String name, String dateColumn, String columns, String orderBy) {}
}
