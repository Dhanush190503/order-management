\# E-Commerce Order Management System



A backend REST API for managing users, products, categories, shopping carts, and orders using Spring Boot, Spring Security, JWT authentication, and MySQL.



\## 🚀 Features



\- User registration and login

\- JWT-based authentication

\- Role-based authorization

\- Admin and customer roles

\- Category management

\- Product management

\- Shopping cart management

\- Order creation and management

\- Order status lifecycle

\- Stock management

\- Automatic stock restoration when an order is cancelled

\- Customer order ownership protection

\- Admin access to all orders

\- Pagination and sorting

\- Input validation

\- Global exception handling

\- Repository, service, controller, and security tests

\- Postman API testing



\## 🛠️ Tech Stack



\- Java 17

\- Spring Boot 4.1.0

\- Spring Security

\- JWT / OAuth2 Resource Server

\- Spring Data JPA

\- Hibernate

\- MySQL 8

\- Maven

\- JUnit

\- Mockito

\- Postman



\## 📁 Project Structure



```text

src/

├── main/

│   ├── java/com/ecommerce/ordermanagement/

│   │   ├── config/

│   │   ├── controller/

│   │   ├── dto/

│   │   ├── entity/

│   │   ├── exception/

│   │   ├── repository/

│   │   └── service/

│   │

│   └── resources/

│       └── application.properties

│

└── test/

&#x20;   └── java/com/ecommerce/ordermanagement/

&#x20;       ├── config/

&#x20;       ├── controller/

&#x20;       ├── repository/

&#x20;       └── service/

Authentication



The application uses JWT Bearer tokens for authentication.



After successful login, the API returns a JWT token.



Use the token in protected requests:



Authorization: Bearer <your-jwt-token>

Roles



The application supports:



CUSTOMER

ADMIN



Customers can manage their own carts and orders.



Administrators can manage products, categories, users, and orders.



⚙️ Local Configuration



Sensitive configuration is intentionally excluded from GitHub.



Create:



src/main/resources/application-local.properties



with your local configuration:



spring.datasource.username=YOUR\_MYSQL\_USERNAME

spring.datasource.password=YOUR\_MYSQL\_PASSWORD



jwt.secret=YOUR\_JWT\_SECRET



admin.email=YOUR\_ADMIN\_EMAIL

admin.password=YOUR\_ADMIN\_PASSWORD



The file is listed in .gitignore and should never be committed.



The main application.properties uses environment-variable placeholders for sensitive values.



🗄️ Database Setup



Create a MySQL database:



CREATE DATABASE ecommerce\_db;



The application uses Spring Data JPA/Hibernate to create and update the required tables.



Default database configuration:



Database: ecommerce\_db

Host: localhost

Port: 3306



Update your local configuration if your MySQL setup is different.



▶️ Running the Application



Make sure MySQL is running and your local configuration is set.



Run using Maven Wrapper:



Windows

.\\mvnw.cmd spring-boot:run



Or:



.\\mvnw.cmd clean package



Then run the generated application.



The application runs on:



http://localhost:8081

🧪 Running Tests



Run the complete test suite with:



.\\mvnw.cmd test



The project includes tests covering:



Repository functionality

Service functionality

Controller behavior

Authentication

Authorization

JWT security

Order ownership

Order status transitions

Validation

Pagination and sorting

📡 API Overview

Authentication

POST /api/auth/register

POST /api/auth/login

Categories

GET    /api/categories

GET    /api/categories/{id}

POST   /api/categories

PUT    /api/categories/{id}

DELETE /api/categories/{id}

Products

GET    /api/products

GET    /api/products/{id}

POST   /api/products

PUT    /api/products/{id}

DELETE /api/products/{id}

Cart

GET    /api/cart

POST   /api/cart/items

PUT    /api/cart/items/{productId}

DELETE /api/cart/items/{productId}

DELETE /api/cart

Orders

POST /api/orders

GET  /api/orders

GET  /api/orders/{id}



GET /api/orders/admin

GET /api/orders/status/{status}



PUT /api/orders/{id}/status/{status}

Users

GET /api/users

GET /api/users/{id}



Additional user endpoints are available for administration.



📦 Order Lifecycle



Orders follow a controlled status flow:



PLACED

&#x20;  ↓

CONFIRMED

&#x20;  ↓

SHIPPED

&#x20;  ↓

DELIVERED



An order can also be cancelled where allowed:



PLACED → CANCELLED



Once an order reaches DELIVERED or CANCELLED, it is treated as a terminal state.



When an order is cancelled, the reserved product stock is restored.



🔒 Authorization



The API enforces role-based access using Spring Security.



Customer



Customers can:



Register and log in

View products and categories

Manage their own cart

Create orders

View their own orders

Access only orders that belong to them

Admin



Administrators can:



Manage products

Manage categories

Manage users

View all orders

Filter orders by status

Update order status

Access individual customer orders



Unauthorized access is rejected by the security layer and application-level ownership checks.



📄 Error Handling



The application provides centralized exception handling for common API errors, including:



Invalid credentials

Resource not found

Unauthorized order access

Invalid request data

Invalid JSON

Invalid pagination/sorting parameters

Unexpected server errors



Responses use a consistent error response structure.



🧪 Postman



The repository includes Postman workspace resources for API testing.



The Postman collection can be used to test:



Authentication

Products

Categories

Users

Cart operations

Orders

Authorization

Pagination

Sorting

Validation

Stock management

Order cancellation



Do not store real passwords, JWTs, API keys, or other secrets in Postman files before committing them.



🔑 Security Notes



Never commit:



application-local.properties



or any file containing:



Database passwords

JWT signing secrets

API keys

Production credentials

User passwords

Access tokens



Use local configuration or environment variables instead.



📌 Project Status



The backend implementation and automated test suite are complete, with coverage across the main authentication, authorization, product, category, cart, and order-management workflows.



👨‍💻 Author



Dhanush



GitHub:



https://github.com/Dhanush190503



