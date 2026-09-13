INSERT INTO companies (user_id, name, country, street_address, state_name, city, postal_code, gstin)
SELECT id, 'Adhithya Chemicals', 'India', '42 Industrial Estate, Peenya', 'Karnataka', 'Bengaluru', '560058', '29ABCDE1234F1Z5'
FROM users WHERE email = 'dhanush.admin@clearly.local'
  AND NOT EXISTS (SELECT 1 FROM companies WHERE user_id = users.id AND name = 'Adhithya Chemicals');

INSERT INTO companies (user_id, name, country, street_address, state_name, city, postal_code, gstin)
SELECT id, 'Clearview Hospitality', 'India', '18 Beach Road', 'Andhra Pradesh', 'Kakinada', '533001', NULL
FROM users WHERE email = 'dhanush.admin@clearly.local'
  AND NOT EXISTS (SELECT 1 FROM companies WHERE user_id = users.id AND name = 'Clearview Hospitality');

INSERT INTO company_addresses (company_id, address_type, street_address, city, state_name, postal_code, country)
SELECT c.id, 'Billing & Shipping', c.street_address, c.city, c.state_name, c.postal_code, c.country
FROM companies c JOIN users u ON u.id = c.user_id
WHERE u.email = 'dhanush.admin@clearly.local' AND c.name = 'Adhithya Chemicals'
  AND NOT EXISTS (SELECT 1 FROM company_addresses a WHERE a.company_id = c.id);

INSERT INTO company_contacts (company_id, first_name, last_name, email, phone_number, contact_role, phone_verified)
SELECT c.id, 'Dhanush', 'DV', 'dhanush.admin@clearly.local', '+91995880528', 'Administrator', TRUE
FROM companies c JOIN users u ON u.id = c.user_id
WHERE u.email = 'dhanush.admin@clearly.local' AND c.name = 'Adhithya Chemicals'
  AND NOT EXISTS (SELECT 1 FROM company_contacts x WHERE x.company_id = c.id);

INSERT INTO company_orders (company_id, order_no, ordered_at, items, amount, status)
SELECT c.id, seed.order_no, seed.ordered_at, seed.items, seed.amount, seed.status
FROM companies c JOIN users u ON u.id = c.user_id
JOIN (
 SELECT 'CLR-260901' order_no, DATE '2026-09-01' ordered_at, 8 items, 18450.00 amount, 'Delivered' status UNION ALL
 SELECT 'CLR-260823', DATE '2026-08-23', 4, 7290.00, 'Delivered' UNION ALL
 SELECT 'CLR-260814', DATE '2026-08-14', 12, 26890.00, 'Delivered' UNION ALL
 SELECT 'CLR-260802', DATE '2026-08-02', 6, 11940.00, 'Delivered' UNION ALL
 SELECT 'CLR-260725', DATE '2026-07-25', 15, 33450.00, 'Delivered' UNION ALL
 SELECT 'CLR-260711', DATE '2026-07-11', 3, 5180.00, 'Delivered' UNION ALL
 SELECT 'CLR-260628', DATE '2026-06-28', 9, 17620.00, 'Delivered' UNION ALL
 SELECT 'CLR-260615', DATE '2026-06-15', 5, 8990.00, 'Delivered' UNION ALL
 SELECT 'CLR-260530', DATE '2026-05-30', 11, 24780.00, 'Delivered' UNION ALL
 SELECT 'CLR-260516', DATE '2026-05-16', 7, 13990.00, 'Delivered' UNION ALL
 SELECT 'CLR-260429', DATE '2026-04-29', 4, 6540.00, 'Delivered' UNION ALL
 SELECT 'CLR-260412', DATE '2026-04-12', 13, 29880.00, 'Delivered' UNION ALL
 SELECT 'CLR-260328', DATE '2026-03-28', 2, 3490.00, 'Delivered' UNION ALL
 SELECT 'CLR-260307', DATE '2026-03-07', 10, 21650.00, 'Delivered'
) seed
WHERE u.email = 'dhanush.admin@clearly.local' AND c.name = 'Adhithya Chemicals'
ON DUPLICATE KEY UPDATE order_no = VALUES(order_no);

INSERT INTO seller_profiles (legal_name,display_name,phone_number,email,address_line1,address_line2,city,state_name,postal_code,country,active)
SELECT 'Adhithya Chemicals','ADHITHYA CHEMICALS','9791046050','adhithyachem@gmail.com','D.No: 1-119-10, Adhithya Nilayam, 1st Floor','Near Ushodaya Junction, Sector 12, MVP Colony','Visakhapatnam','Andhra Pradesh','530017','India',TRUE
WHERE NOT EXISTS (SELECT 1 FROM seller_profiles WHERE legal_name='Adhithya Chemicals');

INSERT INTO company_shipments (company_id, shipped_at, tracking_no, carrier, destination, status)
SELECT c.id, seed.shipped_at, seed.tracking_no, seed.carrier, seed.destination, seed.status
FROM companies c JOIN users u ON u.id = c.user_id
JOIN (
 SELECT DATE '2026-09-02' shipped_at, 'BLD291884120' tracking_no, 'Blue Dart' carrier, 'Bengaluru, Karnataka' destination, 'Delivered' status UNION ALL
 SELECT DATE '2026-08-24', 'DTC884105729', 'Delhivery', 'Bengaluru, Karnataka', 'Delivered' UNION ALL
 SELECT DATE '2026-08-15', 'EXB661092145', 'Ecom Express', 'Bengaluru, Karnataka', 'Delivered'
) seed
WHERE u.email = 'dhanush.admin@clearly.local' AND c.name = 'Adhithya Chemicals'
ON DUPLICATE KEY UPDATE tracking_no = VALUES(tracking_no);

INSERT INTO company_invoices (company_id, invoice_no, invoice_date, items, amount, status)
SELECT c.id, seed.invoice_no, seed.invoice_date, seed.items, seed.amount, seed.status
FROM companies c JOIN users u ON u.id = c.user_id
JOIN (
 SELECT 'INV-260901' invoice_no, DATE '2026-09-01' invoice_date, 8 items, 18450.00 amount, 'Paid' status UNION ALL
 SELECT 'INV-260823', DATE '2026-08-23', 4, 7290.00, 'Paid' UNION ALL
 SELECT 'INV-260814', DATE '2026-08-14', 12, 26890.00, 'Paid'
) seed
WHERE u.email = 'dhanush.admin@clearly.local' AND c.name = 'Adhithya Chemicals'
ON DUPLICATE KEY UPDATE invoice_no = VALUES(invoice_no);

INSERT INTO company_ledger_entries (company_id, entry_date, document_no, debit, credit, balance)
SELECT c.id, seed.entry_date, seed.document_no, seed.debit, seed.credit, seed.balance
FROM companies c JOIN users u ON u.id = c.user_id
JOIN (
 SELECT DATE '2026-09-01' entry_date, 'INV-260901' document_no, 18450.00 debit, 0.00 credit, 18450.00 balance UNION ALL
 SELECT DATE '2026-09-03', 'PAY-260903', 0.00, 18450.00, 0.00 UNION ALL
 SELECT DATE '2026-08-23', 'INV-260823', 7290.00, 0.00, 7290.00 UNION ALL
 SELECT DATE '2026-08-25', 'PAY-260825', 0.00, 7290.00, 0.00
) seed
WHERE u.email = 'dhanush.admin@clearly.local' AND c.name = 'Adhithya Chemicals'
  AND NOT EXISTS (SELECT 1 FROM company_ledger_entries l WHERE l.company_id = c.id AND l.document_no = seed.document_no);
