# ROMS - Restaurant Operations Management System

A comprehensive restaurant management system with a Java-based frontend and MySQL database backend.

## Features

- **Menu Management**: Organize and maintain your restaurant's menu items and categories
- **Order Processing**: Track and manage customer orders and their status
- **Kitchen Operations**: Manage kitchen workflows and menu item preparation
- **Admin Dashboard**: Complete control over restaurant operations
- **Customer Interface**: User-friendly ordering experience 
- **Inventory Management**: 
  - Track ingredient stock levels
  - Manage suppliers
  - Create and track purchase orders
  - Automated inventory usage tracking
  - Threshold alerts for reordering

## System Architecture

### Frontend
- Java-based application using JavaFX for the UI
- MVC architecture for clean separation of concerns
- Custom CSS styling for a modern user experience

### Backend
- MySQL database with comprehensive schema
- Core entities: Menu Items, Categories, Ingredients, Orders, Customers
- Inventory tracking: Current stock levels, transaction history
- Supplier management: Supplier information and purchase orders
- Relational tables for many-to-many relationships

## Getting Started

1. Import the `restaurant_schema.sql` file into your MySQL environment
2. Configure database connection in your application
3. Run the application using the provided `run.sh` script
4. Start using the system to manage your restaurant operations

## User Interfaces

- **Admin Dashboard**: Complete restaurant management
- **Kitchen Dashboard**: Order management and preparation tracking
- **Customer View**: Menu browsing and ordering interface
- **Login System**: Secure access control

## Schema Diagram

(Schema diagram to be added)

## License

This project is licensed under the MIT License - see the LICENSE file for details. 