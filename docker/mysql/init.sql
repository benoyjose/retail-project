-- Retail Microservices System: Multi-database bootstrap script
CREATE DATABASE IF NOT EXISTS customer_db;
CREATE DATABASE IF NOT EXISTS product_db;
CREATE DATABASE IF NOT EXISTS cart_db;
CREATE DATABASE IF NOT EXISTS order_db;
CREATE DATABASE IF NOT EXISTS payment_db;
CREATE DATABASE IF NOT EXISTS inventory_db;
CREATE DATABASE IF NOT EXISTS review_db;
CREATE DATABASE IF NOT EXISTS campaign_db;
CREATE DATABASE IF NOT EXISTS support_db;
CREATE DATABASE IF NOT EXISTS wishlist_db;

-- Grant all privileges to application user
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%';
FLUSH PRIVILEGES;

-- =========================================================================
-- 1. Seed Customer Database (Demo Customer & Store Manager)
-- Passwords hashed with BCrypt (Password: 'Password@123')
-- =========================================================================
USE customer_db;

CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(60) NOT NULL,
    last_name VARCHAR(60) NOT NULL,
    phone VARCHAR(500),
    shipping_address VARCHAR(1000),
    role VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO customers (id, email, password_hash, first_name, last_name, phone, shipping_address, role, active)
VALUES 
(1, 'john.doe@example.com', '$2a$12$046Z6yRzRj83O4sWkWKj3OG724U9L1XnQk8i5N2.mB.r127F8P4aO', 'John', 'Doe', '+1-555-123-4567', '123 Market St, San Francisco, CA 94105', 'ROLE_CUSTOMER', TRUE),
(2, 'manager@retail.com', '$2a$12$046Z6yRzRj83O4sWkWKj3OG724U9L1XnQk8i5N2.mB.r127F8P4aO', 'Sarah', 'Connor', '+1-555-987-6543', 'HQ Retail Plaza, Seattle, WA 98101', 'ROLE_MANAGER', TRUE)
ON DUPLICATE KEY UPDATE email=email;

-- =========================================================================
-- 2. Seed Product Database (Categories & Products)
-- =========================================================================
USE product_db;

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    parent_id BIGINT
);

CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    base_price DECIMAL(12, 2) NOT NULL,
    brand VARCHAR(100),
    image_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    category_id BIGINT NOT NULL,
    average_rating DOUBLE DEFAULT 0.0,
    review_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

INSERT INTO categories (id, name, description, parent_id)
VALUES 
(1, 'Electronics', 'Consumer electronics, computers, and smart devices', NULL),
(2, 'Audio & Accessories', 'Headphones, earbuds, and premium sound systems', 1),
(3, 'Apparel', 'Men and women fashion, sportswear, and footwear', NULL)
ON DUPLICATE KEY UPDATE name=name;

INSERT INTO products (id, sku, name, description, base_price, brand, image_url, active, category_id, average_rating, review_count)
VALUES 
(1, 'SKU-PROD-001', 'Dell XPS 15 Gaming Laptop', '15.6 inch OLED Display, 32GB RAM, 1TB NVMe SSD, RTX 4070', 1899.99, 'Dell', 'https://images.unsplash.com/photo-1593642632823-8f785ba67e45', TRUE, 1, 4.8, 124),
(2, 'SKU-PROD-002', 'Sony WH-1000XM5 Wireless Headphones', 'Industry-leading noise canceling with auto NC optimizer', 399.99, 'Sony', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e', TRUE, 2, 4.9, 310),
(3, 'SKU-PROD-003', 'Apple iPhone 15 Pro Max', '256GB Titanium, A17 Pro Chip, 48MP Triple Lens Camera', 1199.99, 'Apple', 'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5', TRUE, 1, 4.7, 520),
(4, 'SKU-PROD-004', 'Nike Air Zoom Pegasus 40', 'Responsive running shoes with breathable engineered mesh', 129.99, 'Nike', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff', TRUE, 3, 4.6, 88)
ON DUPLICATE KEY UPDATE sku=sku;

-- =========================================================================
-- 3. Seed Inventory Database (Stock & Thresholds)
-- =========================================================================
USE inventory_db;

CREATE TABLE IF NOT EXISTS inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_sku VARCHAR(64) NOT NULL UNIQUE,
    available_stock INT NOT NULL DEFAULT 0,
    reserved_stock INT NOT NULL DEFAULT 0,
    low_stock_threshold INT NOT NULL DEFAULT 10,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO inventory (id, product_sku, available_stock, reserved_stock, low_stock_threshold, version)
VALUES 
(1, 'SKU-PROD-001', 50, 0, 10, 0),
(2, 'SKU-PROD-002', 150, 0, 20, 0),
(3, 'SKU-PROD-003', 75, 0, 15, 0),
(4, 'SKU-PROD-004', 200, 0, 30, 0)
ON DUPLICATE KEY UPDATE product_sku=product_sku;

-- =========================================================================
-- 4. Seed Campaign Database (Active Seasonal Sales)
-- =========================================================================
USE campaign_db;

CREATE TABLE IF NOT EXISTS campaigns (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaign_code VARCHAR(64) NOT NULL UNIQUE,
    title VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    discount_percentage DECIMAL(5, 2) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS campaign_skus (
    campaign_id BIGINT NOT NULL,
    sku VARCHAR(64) NOT NULL,
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id)
);

CREATE TABLE IF NOT EXISTS campaign_categories (
    campaign_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id)
);

INSERT INTO campaigns (id, campaign_code, title, description, discount_percentage, start_date, end_date, status)
VALUES 
(1, 'SUMMER2026', 'Summer Mega Electronics Sale', '20% off all flagship laptops and mobile devices', 20.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 'ACTIVE'),
(2, 'AUDIO10', 'Premium Audio Discount Week', '10% instant discount on high-fidelity audio equipment', 10.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 'ACTIVE')
ON DUPLICATE KEY UPDATE campaign_code=campaign_code;

INSERT INTO campaign_skus (campaign_id, sku)
VALUES 
(1, 'SKU-PROD-001'),
(1, 'SKU-PROD-003'),
(2, 'SKU-PROD-002');

-- =========================================================================
-- 5. Seed Review Database (Verified Buyer Reviews)
-- =========================================================================
USE review_db;

CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    product_sku VARCHAR(64) NOT NULL,
    rating INT NOT NULL,
    title VARCHAR(120),
    comment TEXT NOT NULL,
    verified_purchase BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO reviews (id, customer_id, product_sku, rating, title, comment, verified_purchase)
VALUES 
(1, 1, 'SKU-PROD-001', 5, 'Unmatched Workstation Power', 'Handles 4K video rendering and heavy software development with zero thermal throttling.', TRUE),
(2, 1, 'SKU-PROD-002', 5, 'Best Noise Cancelling on the Market', 'Battery easily lasts 30 hours. Soundstage is crisp and bass is balanced.', TRUE)
ON DUPLICATE KEY UPDATE id=id;
