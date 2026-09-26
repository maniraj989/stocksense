# Viva Preparation Guide

## OOP
- Inheritance: `Admin extends User`, `Staff extends User`
- Encapsulation: private fields with getter and setter methods
- Polymorphism: service interfaces can be implemented by concrete service classes
- Abstraction: domain models hide data storage details and expose meaningful behavior

## JDBC
- JDBC is used to connect Java to MySQL.
- PreparedStatement is used to avoid SQL injection.
- A transaction ensures consistent updates for purchase and sale flows.
- DAO classes keep database code separate from Swing code.

## Swing
- Swing is used for all GUI screens.
- `JFrame` is the main window.
- `JTable` displays product or supplier data.
- Event listeners handle button clicks and form submission.

## Multithreading
- A monitoring service can run in a separate thread while the GUI remains responsive.
- This is useful for checking low stock, expiry alerts, and reorder recommendations.

## Exception Handling
- `InsufficientStockException` handles invalid sale quantities.
- `InvalidProductException` handles invalid product data.
- `ProductNotFoundException` is raised when a product is not available.

## Smart Feature
- Reorder recommendation is calculated by comparing current stock with minimum stock and simple estimated days remaining.
- The system avoids machine learning and uses simple Java logic that students can explain on a board.

## Why use role-based access?
- Admin can manage sensitive actions.
- Staff can do day-to-day operations without admin-only privileges.
- This improves control and matches academic project requirements.
