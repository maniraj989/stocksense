CREATE DATABASE IF NOT EXISTS smart_inventory;
USE smart_inventory;

DROP TABLE IF EXISTS alerts;
DROP TABLE IF EXISTS stock_movements;
DROP TABLE IF EXISTS sale_items;
DROP TABLE IF EXISTS purchase_items;
DROP TABLE IF EXISTS sales;
DROP TABLE IF EXISTS purchases;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS suppliers;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'STAFF') NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE suppliers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    company VARCHAR(150),
    phone VARCHAR(30),
    email VARCHAR(100),
    address VARCHAR(255)
);

CREATE TABLE products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    product_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(100),
    description TEXT,
    supplier_id INT,
    purchase_price DECIMAL(10,2) NOT NULL,
    selling_price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    minimum_stock INT NOT NULL DEFAULT 0,
    maximum_stock INT NOT NULL DEFAULT 0,
    expiry_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL
);

CREATE TABLE purchases (
    id INT PRIMARY KEY AUTO_INCREMENT,
    supplier_id INT NOT NULL,
    purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(12,2) NOT NULL,
    created_by INT,
    CONSTRAINT fk_purchases_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE RESTRICT,
    CONSTRAINT fk_purchases_user FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE purchase_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    purchase_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_purchase_items_purchase FOREIGN KEY (purchase_id) REFERENCES purchases(id) ON DELETE CASCADE,
    CONSTRAINT fk_purchase_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
);

CREATE TABLE sales (
    id INT PRIMARY KEY AUTO_INCREMENT,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(12,2) NOT NULL,
    created_by INT,
    CONSTRAINT fk_sales_user FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE sale_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    sale_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_sale_items_sale FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
    CONSTRAINT fk_sale_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
);

CREATE TABLE stock_movements (
    id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    movement_type ENUM('IN', 'OUT', 'ADJUSTMENT') NOT NULL,
    quantity INT NOT NULL,
    reference_id INT,
    movement_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INT,
    CONSTRAINT fk_stock_movements_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_stock_movements_user FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE alerts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    alert_type ENUM('LOW_STOCK', 'EXPIRY', 'REORDER') NOT NULL,
    message VARCHAR(255) NOT NULL,
    severity ENUM('LOW', 'MEDIUM', 'HIGH') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('OPEN', 'RESOLVED') DEFAULT 'OPEN',
    CONSTRAINT fk_alerts_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

INSERT INTO users (name, username, password, role, email) VALUES
('Admin User', 'admin', 'admin123', 'ADMIN', 'admin@inventory.com'),
('Staff User', 'staff', 'staff123', 'STAFF', 'staff@inventory.com');

INSERT INTO suppliers (name, company, phone, email, address) VALUES
('Ramesh Kumar', 'TechCore Supplies', '9876543210', 'sales@techcore.in', 'Bengaluru, Karnataka'),
('Priya Sharma', 'Stationery Hub', '9123456780', 'support@stationeryhub.in', 'Pune, Maharashtra'),
('Amit Verma', 'FreshMart Distributors', '9988776655', 'amit@freshmart.in', 'Delhi, India');

INSERT INTO products (product_code, name, category, description, supplier_id, purchase_price, selling_price, quantity, minimum_stock, maximum_stock, expiry_date) VALUES
('P1001', 'Logitech Keyboard', 'Electronics', 'Wireless keyboard', 1, 1200.00, 1800.00, 8, 10, 30, NULL),
('P1002', 'Milk Powder', 'Grocery', 'Family milk powder', 3, 350.00, 500.00, 15, 10, 40, '2026-09-20'),
('P1003', 'Notebook A4', 'Stationery', '200 pages notebook', 2, 45.00, 75.00, 25, 12, 50, NULL),
('P1004', 'USB Mouse', 'Electronics', 'Optical USB mouse', 1, 450.00, 700.00, 4, 8, 20, NULL),
('P1005', 'Pen Pack', 'Stationery', 'Pack of 10 pens', 2, 60.00, 100.00, 0, 5, 30, NULL);

INSERT INTO purchases (supplier_id, purchase_date, total_amount, created_by) VALUES
(1, '2026-08-20 09:30:00', 2400.00, 1),
(2, '2026-08-21 11:00:00', 540.00, 1),
(3, '2026-08-22 16:45:00', 1050.00, 1);

INSERT INTO purchase_items (purchase_id, product_id, quantity, unit_price, subtotal) VALUES
(1, 1, 10, 1200.00, 12000.00),
(2, 3, 20, 45.00, 900.00),
(3, 2, 15, 350.00, 5250.00);

INSERT INTO sales (sale_date, total_amount, created_by) VALUES
('2026-08-23 10:00:00', 1800.00, 2),
('2026-08-23 12:15:00', 700.00, 2),
('2026-08-24 13:00:00', 1500.00, 2);

INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, subtotal) VALUES
(1, 1, 2, 1800.00, 3600.00),
(2, 4, 1, 700.00, 700.00),
(3, 3, 10, 75.00, 750.00);

INSERT INTO stock_movements (product_id, movement_type, quantity, reference_id, movement_date, created_by) VALUES
(1, 'IN', 10, 1, '2026-08-20 09:30:00', 1),
(3, 'IN', 20, 2, '2026-08-21 11:00:00', 1),
(2, 'IN', 15, 3, '2026-08-22 16:45:00', 1),
(1, 'OUT', 2, 1, '2026-08-23 10:00:00', 2),
(4, 'OUT', 1, 2, '2026-08-23 12:15:00', 2),
(3, 'OUT', 10, 3, '2026-08-24 13:00:00', 2);

INSERT INTO alerts (product_id, alert_type, message, severity, status) VALUES
(1, 'LOW_STOCK', 'Logitech Keyboard stock is below minimum threshold.', 'HIGH', 'OPEN'),
(4, 'LOW_STOCK', 'USB Mouse stock is below minimum threshold.', 'HIGH', 'OPEN'),
(5, 'LOW_STOCK', 'Pen Pack is out of stock.', 'MEDIUM', 'OPEN'),
(2, 'EXPIRY', 'Milk Powder expires within 27 days.', 'MEDIUM', 'OPEN');
