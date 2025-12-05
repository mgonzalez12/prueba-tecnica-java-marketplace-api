-- Categories
INSERT INTO categories (name, description, code, active, created_at, updated_at) 
VALUES ('Electronics', 'Electronic devices', 'ELEC', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO categories ( name, description, code, active, parent_id, created_at, updated_at) 
VALUES ('Smartphones', 'Mobile phones', 'SMART', true, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO categories ( name, description, code, active, parent_id, created_at, updated_at) 
VALUES ( 'Laptops', 'Notebook computers', 'LAPT', true, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Products
INSERT INTO products ( sku, name, description, price, promotional_price, category_id, active, is_external, created_at, updated_at) 
VALUES ('IPHONE-15', 'iPhone 15', 'Apple iPhone 15 128GB', 999.99, 899.99, 2, true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO products ( sku, name, description, price, promotional_price, category_id, active, is_external, created_at, updated_at) 
VALUES ( 'MACBOOK-PRO', 'MacBook Pro M3', 'Apple MacBook Pro M3 14"', 1599.99, null, 3, true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Inventory
INSERT INTO inventory (product_id, quantity, reserved_quantity, last_updated)
VALUES (1, 50, 0, CURRENT_TIMESTAMP);

INSERT INTO inventory (product_id, quantity, reserved_quantity, last_updated)
VALUES ( 2, 20, 0, CURRENT_TIMESTAMP);

-- Customers
-- CustomerEntity fields: id, first_name, last_name, email, status, birth_date, registration_date, last_activity_date, renewal_date, phone, address, total_orders, total_spent
-- Let the database generate IDs automatically (do NOT insert explicit id values)
INSERT INTO customers (first_name, last_name, email, status, birth_date, registration_date, phone, address, total_orders, total_spent)
VALUES ('John', 'Doe', 'john.doe@example.com', 'ACTIVE', '1990-01-01', CURRENT_TIMESTAMP, '+1234567890', '123 Main St', 0, 0);

INSERT INTO customers (first_name, last_name, email, status, birth_date, registration_date, phone, address, total_orders, total_spent)
VALUES ('Jane', 'Smith', 'jane.smith@example.com', 'ACTIVE', '1995-05-15', CURRENT_TIMESTAMP, '+0987654321', '456 Second St', 0, 0);

