# SmartMart

Backend for a supermarket app - built with Spring Boot. Handles products, customers, orders, cart, inventory, payments, discounts, reviews, notifications, wishlist... basically everything a small e-commerce backend needs. JWT auth, two roles (admin and normal user).

## Why I built this

The idea was a supermarket that was still doing stock tracking manually and had no way to sell online. This API is meant to be the backend for that - a frontend (web or mobile) can just hit these endpoints.

## Stack

Java 21, Spring Boot 3.2.5, Spring Security, Spring Data JPA + Hibernate, MySQL, JWT (jjwt), Lombok, Swagger for docs. Built with Maven.

## Structure

Pretty standard layers - Controller -> Service -> Repository. Controllers don't have any logic in them, it's all in the service classes. DTOs everywhere so I'm never returning entities directly from a controller. Passwords are BCrypt hashed, auth runs through a JWT filter before Spring Security checks the request.

## What it does

- register/login with JWT
- admin vs user roles
- products - search, filter, pagination
- categories + suppliers
- inventory with stock history and low stock warnings
- cart (checks stock before letting you add)
- orders - deducts stock automatically when placed
- payments (cash/card/online, just tracking not actually processing)
- discounts that can be tied to specific products
- reviews (1-5 stars)
- saved addresses per customer
- notifications
- wishlist
- small admin dashboard for stats
- consistent error responses everywhere (one exception handler for the whole app)
- validation on all the request bodies

## DB

22 tables. Main ones: users, roles, customers, products, categories, suppliers, inventory + inventory_transactions, carts + cart_items, orders + order_items, payments, discounts + product_discounts, reviews, addresses, notifications, wishlists + wishlist_items, plus a user_roles join table.

## Auth

Register -> hash password, create User + Customer, hand back a token.
Login -> check credentials, hand back a token.

After that every request just needs `Authorization: Bearer <token>` in the header.

Roles:
- not logged in - can browse products/categories and search
- USER - all of the above + cart, orders, reviews, addresses, wishlist, notifications
- ADMIN - all of the above + can manage products, categories, suppliers, inventory, discounts, customers, orders, payments, and see the dashboard

## Endpoints

Auth (public):
```
POST /api/auth/register
POST /api/auth/login
```

Products (GET is open to everyone, writes need admin):
```
GET    /api/products
GET    /api/products/{id}
GET    /api/products/search?keyword=
POST   /api/products
PUT    /api/products/{id}
PATCH  /api/products/{id}
DELETE /api/products/{id}
```

Categories (same pattern):
```
GET    /api/categories
GET    /api/categories/{id}
POST   /api/categories
PUT    /api/categories/{id}
PATCH  /api/categories/{id}
DELETE /api/categories/{id}
```

Suppliers (admin only, all of it):
```
GET    /api/suppliers
GET    /api/suppliers/{id}
POST   /api/suppliers
PUT    /api/suppliers/{id}
DELETE /api/suppliers/{id}
```

Inventory (admin only):
```
GET    /api/inventory/product/{productId}
PATCH  /api/inventory/product/{productId}/adjust
GET    /api/inventory/low-stock
GET    /api/inventory/product/{productId}/transactions
```

Customers:
```
GET    /api/customers            - admin
GET    /api/customers/{id}       - admin
GET    /api/customers/me         - logged in user
PUT    /api/customers/{id}       - admin
PATCH  /api/customers/me         - logged in user
DELETE /api/customers/{id}       - admin
```

Cart (needs login):
```
GET    /api/cart
POST   /api/cart/items
PUT    /api/cart/items/{cartItemId}
DELETE /api/cart/items/{cartItemId}
DELETE /api/cart/clear
```

Orders:
```
POST   /api/orders              - user places order
GET    /api/orders               - user's own orders
GET    /api/orders/{id}          - user or admin
PATCH  /api/orders/{id}/status   - admin updates status
DELETE /api/orders/{id}          - user cancels
```

Payments:
```
GET    /api/payments/order/{orderId}   - user or admin
PATCH  /api/payments/order/{orderId}   - admin
```

Discounts (admin):
```
GET    /api/discounts
GET    /api/discounts/{id}
POST   /api/discounts
PUT    /api/discounts/{id}
PATCH  /api/discounts/{id}/toggle
POST   /api/discounts/{discountId}/products/{productId}
DELETE /api/discounts/{discountId}/products/{productId}
DELETE /api/discounts/{id}
```

Reviews:
```
GET    /api/reviews/product/{productId}   - public, anyone can read
POST   /api/reviews                       - logged in user
PUT    /api/reviews/{id}                  - logged in user
DELETE /api/reviews/{id}                  - logged in user
```

Addresses (needs login):
```
GET    /api/addresses
GET    /api/addresses/{id}
POST   /api/addresses
PUT    /api/addresses/{id}
PATCH  /api/addresses/{id}
DELETE /api/addresses/{id}
```

Notifications:
```
GET    /api/notifications
PATCH  /api/notifications/{id}/read
PATCH  /api/notifications/read-all
```

Wishlist:
```
GET    /api/wishlist
POST   /api/wishlist/items/{productId}
DELETE /api/wishlist/items/{productId}
```

Admin dashboard:
```
GET    /api/admin/dashboard
GET    /api/admin/inventory-summary
```

## Running it

Need Java 21, MySQL 8, and Maven (or just use `mvnw.cmd` if Maven isn't installed).

Create the DB:
```sql
CREATE DATABASE smartmart CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Config is in `application.properties`. Can override these with env vars if needed:
```
DB_USERNAME=root
DB_PASSWORD=root
JWT_SECRET=SmartMartSuperSecretKeyForJWTTokenGenerationMustBe256BitsLong!
JWT_EXPIRATION_MS=86400000
```

Run:
```bash
mvn spring-boot:run
```
or on Windows without maven:
```bash
mvnw.cmd spring-boot:run
```

Runs on `localhost:8080`. Swagger docs at `localhost:8080/swagger-ui.html`.

## Test logins

Admin - admin@smartmart.com / Admin@1234
User - user@smartmart.com / User@1234

## Testing in Postman

Login with one of the accounts above, grab the accessToken from the response, stick it in the Authorization header as `Bearer <token>`, then you can hit the protected routes.

## TODO / ideas for later

- email when an order is placed
- email alert for low stock
- barcode/QR for products
- some kind of sales analytics
- multi-currency
- shipping integration
# smart_mart
