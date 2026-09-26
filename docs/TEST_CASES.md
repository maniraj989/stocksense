# Test Cases for Smart Inventory Management System

## 1. Login tests
1. Valid admin credentials should log in successfully.
2. Valid staff credentials should log in successfully.
3. Wrong username should display a friendly error.
4. Wrong password should display a friendly error.

## 2. Product management tests
1. Add a valid product with all required fields.
2. Reject a product with empty product code.
3. Reject a product with negative quantity.
4. Update an existing product and verify the new values.
5. Delete a product and verify it is removed.

## 3. Supplier tests
1. Add a new supplier.
2. Verify supplier appears in the supplier table.
3. Update supplier email and phone.
4. Delete supplier if needed.

## 4. Purchase flow tests
1. Select supplier and product.
2. Enter quantity and save purchase.
3. Verify stock increases after purchase.
4. Verify a stock movement row is created for IN movement.

## 5. Sales flow tests
1. Select a product with enough stock.
2. Save sale and verify stock decreases.
3. Attempt a sale greater than available stock.
4. Verify `InsufficientStockException` triggers a friendly message.

## 6. Smart feature tests
1. Make stock less than or equal to minimum stock.
2. Verify a low-stock alert is shown.
3. Set expiry date to 15 days away.
4. Verify expiry alert appears.
5. Check reorder recommendation logic for products with low stock.

## 7. Dashboard/report tests
1. Total products count updates correctly.
2. Today's sales total matches actual sale records.
3. Report cards show correct values.
4. Low-stock and expiry sections reflect database values.

## 8. Role access tests
1. Admin can access all management screens.
2. Staff cannot access admin-only operations.
3. Logout returns to the login page.

## 9. Database tests
1. Database connection should be successful.
2. PreparedStatement must be used instead of concatenating SQL strings.
3. Transactions should roll back on failure.
