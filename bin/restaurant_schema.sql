-- Create the database
CREATE DATABASE restaurant_db;
USE restaurant_db;

-- Create the Staff table (combines Manager and Kitchen staff)
CREATE TABLE Staff (
    staff_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    role ENUM('manager', 'kitchen_staff') NOT NULL,
    kitchen_id INT UNIQUE
);

-- Create the Category table
CREATE TABLE Category (
    title VARCHAR(50) PRIMARY KEY
);

-- Create the MenuItem table
CREATE TABLE MenuItem (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    price DOUBLE NOT NULL,
    quantity INT DEFAULT 1,
    category_title VARCHAR(50),
    image_path VARCHAR(255),
    kitchen_id INT,
    FOREIGN KEY (category_title) REFERENCES Category(title),
    FOREIGN KEY (kitchen_id) REFERENCES Staff(kitchen_id)
);

-- Create the Ingredient table
CREATE TABLE Ingredient (
    title VARCHAR(100) PRIMARY KEY,
    current_quantity DECIMAL(10,2) NOT NULL,
    unit_of_measure VARCHAR(20) NOT NULL,
    min_threshold DECIMAL(10,2) NOT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create the Supplier table
CREATE TABLE Supplier (
    supplier_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(255)
);

-- Create the Purchase_Order table (simplified)
CREATE TABLE Purchase_Order (
    po_id INT PRIMARY KEY AUTO_INCREMENT,
    supplier_id INT NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('draft', 'ordered', 'completed', 'cancelled') DEFAULT 'draft',
    total_amount DECIMAL(10,2),
    created_by VARCHAR(50),
    FOREIGN KEY (supplier_id) REFERENCES Supplier(supplier_id),
    FOREIGN KEY (created_by) REFERENCES Staff(staff_id)
);

-- Create the Customer table
CREATE TABLE Customer (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    address VARCHAR(255),
    table_number INT,
    to_deliver BOOLEAN DEFAULT FALSE
);

-- Create the Orders table (renamed from Order)
CREATE TABLE Orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    status VARCHAR(50) NOT NULL,
    date DATE NOT NULL,
    customer_id INT,
    staff_id VARCHAR(50),
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id),
    FOREIGN KEY (staff_id) REFERENCES Staff(staff_id)
);

-- Create the Order_Items table (simplified)
CREATE TABLE Order_Items (
    order_id INT,
    item_id INT,
    quantity INT DEFAULT 1,
    PRIMARY KEY (order_id, item_id),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    FOREIGN KEY (item_id) REFERENCES MenuItem(item_id)
);

-- Create the Feedback table
CREATE TABLE Feedback (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    comment TEXT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    submission_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create the Users table
CREATE TABLE Users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(225) NOT NULL,
    role ENUM('manager', 'kitchen') NOT NULL,
    mot_de_pass VARCHAR(100) NOT NULL
);
