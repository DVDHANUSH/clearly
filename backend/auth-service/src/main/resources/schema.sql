CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(254) NULL UNIQUE,
    phone_number VARCHAR(20) NULL UNIQUE,
    full_name VARCHAR(120) NOT NULL,
    password_hash VARCHAR(100) NULL,
    auth_provider VARCHAR(24) NOT NULL DEFAULT 'LOCAL',
    google_subject VARCHAR(255) NULL UNIQUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    role VARCHAR(30) NOT NULL DEFAULT 'CUSTOMER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS user_otp_verifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    purpose VARCHAR(30) NOT NULL DEFAULT 'SIGNUP',
    code_hash VARCHAR(100) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    consumed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_otp_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_otp_latest (user_id, purpose, created_at)
);

CREATE TABLE IF NOT EXISTS companies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(160) NOT NULL,
    country VARCHAR(80) NOT NULL DEFAULT 'India',
    street_address VARCHAR(255) NOT NULL,
    state_name VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(12) NOT NULL,
    gstin VARCHAR(20) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_company_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_company_user (user_id)
);

CREATE TABLE IF NOT EXISTS company_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT NOT NULL,
    order_no VARCHAR(40) NOT NULL UNIQUE,
    ordered_at DATE NOT NULL,
    items INT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    CONSTRAINT fk_company_order FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    INDEX idx_orders_company_date (company_id, ordered_at)
);

CREATE TABLE IF NOT EXISTS company_shipments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT NOT NULL,
    shipped_at DATE NOT NULL,
    tracking_no VARCHAR(60) NOT NULL UNIQUE,
    carrier VARCHAR(80) NOT NULL,
    destination VARCHAR(180) NOT NULL,
    status VARCHAR(30) NOT NULL,
    CONSTRAINT fk_company_shipment FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    INDEX idx_shipments_company_date (company_id, shipped_at)
);

CREATE TABLE IF NOT EXISTS company_invoices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT NOT NULL,
    invoice_no VARCHAR(40) NOT NULL UNIQUE,
    invoice_date DATE NOT NULL,
    items INT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    CONSTRAINT fk_company_invoice FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    INDEX idx_invoices_company_date (company_id, invoice_date)
);

CREATE TABLE IF NOT EXISTS company_ledger_entries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT NOT NULL,
    entry_date DATE NOT NULL,
    document_no VARCHAR(60) NOT NULL,
    debit DECIMAL(12,2) NOT NULL DEFAULT 0,
    credit DECIMAL(12,2) NOT NULL DEFAULT 0,
    balance DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_company_ledger FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    INDEX idx_ledger_company_date (company_id, entry_date)
);

CREATE TABLE IF NOT EXISTS company_addresses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT NOT NULL,
    address_type VARCHAR(30) NOT NULL DEFAULT 'Shipping',
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state_name VARCHAR(100) NOT NULL,
    postal_code VARCHAR(12) NOT NULL,
    country VARCHAR(80) NOT NULL DEFAULT 'India',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_company_address FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    INDEX idx_addresses_company (company_id)
);

CREATE TABLE IF NOT EXISTS company_contacts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT NOT NULL,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NULL,
    email VARCHAR(254) NULL,
    phone_number VARCHAR(20) NOT NULL,
    contact_role VARCHAR(80) NULL,
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_company_contact FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    INDEX idx_contacts_company (company_id)
);

CREATE TABLE IF NOT EXISTS user_cart_items (
    user_id BIGINT NOT NULL,
    product_ref VARCHAR(100) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, product_ref),
    CONSTRAINT fk_user_cart_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_user_cart_quantity CHECK (quantity > 0),
    INDEX idx_user_cart_updated (user_id, updated_at)
);

CREATE TABLE IF NOT EXISTS user_wishlist_items (
    user_id BIGINT NOT NULL,
    product_ref VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, product_ref),
    CONSTRAINT fk_user_wishlist_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_wishlist_created (user_id, created_at)
);

CREATE TABLE IF NOT EXISTS seller_profiles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    legal_name VARCHAR(180) NOT NULL,
    display_name VARCHAR(180) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    email VARCHAR(254) NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255) NULL,
    city VARCHAR(100) NOT NULL,
    state_name VARCHAR(100) NOT NULL,
    postal_code VARCHAR(12) NOT NULL,
    country VARCHAR(80) NOT NULL DEFAULT 'India',
    gstin VARCHAR(20) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS customer_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    order_no VARCHAR(40) NOT NULL UNIQUE,
    invoice_no VARCHAR(40) NULL UNIQUE,
    razorpay_order_id VARCHAR(80) NULL UNIQUE,
    subtotal DECIMAL(12,2) NOT NULL,
    shipping_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    payment_status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
    order_status VARCHAR(24) NOT NULL DEFAULT 'PAYMENT_PENDING',
    billing_name VARCHAR(180) NOT NULL,
    billing_phone VARCHAR(20) NOT NULL,
    billing_email VARCHAR(254) NULL,
    billing_address VARCHAR(255) NOT NULL,
    billing_city VARCHAR(100) NOT NULL,
    billing_state VARCHAR(100) NOT NULL,
    billing_postal_code VARCHAR(12) NOT NULL,
    billing_gstin VARCHAR(20) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP NULL,
    CONSTRAINT fk_customer_order_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_customer_order_seller FOREIGN KEY (seller_id) REFERENCES seller_profiles(id),
    INDEX idx_customer_orders_user_date (user_id, created_at)
);

CREATE TABLE IF NOT EXISTS customer_order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_ref VARCHAR(100) NOT NULL,
    product_name VARCHAR(180) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    line_total DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_customer_order_item FOREIGN KEY (order_id) REFERENCES customer_orders(id) ON DELETE CASCADE,
    CONSTRAINT chk_customer_order_item_quantity CHECK (quantity > 0)
);

CREATE TABLE IF NOT EXISTS payment_transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    provider VARCHAR(30) NOT NULL DEFAULT 'RAZORPAY',
    provider_payment_id VARCHAR(100) NOT NULL UNIQUE,
    provider_order_id VARCHAR(100) NOT NULL,
    signature_hash VARCHAR(128) NOT NULL,
    status VARCHAR(24) NOT NULL,
    verified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES customer_orders(id),
    INDEX idx_payment_order (order_id)
);
