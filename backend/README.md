# Clearly Store backend

This backend follows the independent-service layout used by the supplied
`Microservices_CWD` project. Each service is a standalone Spring Boot Maven
application with the same familiar package split:

`config` → `controllers` → `dtos` → `entities` → `repositories` → `services`

## Services

| Service | Port | Responsibility |
|---|---:|---|
| `discovery-service` | 8761 | Eureka service registry |
| `config-server` | 8888 | Central service configuration |
| `gateway-service` | 8080 | Public API entry point and security boundary |
| `catalog-service` | 8081 | Products, brands, categories, images, sizes, discounts, documents and specifications |
| `order-service` | 8083 | Order lifecycle and checkout integration point |
| `notification-service` | 8084 | Order and account notification events |
| `auth-service` | 8085 | Authentication, JWT issuance, and authenticated customer shopping state |

The catalog service is the replacement for the reference project's course and
category domain. It keeps the same controller/DTO/entity/repository/service
style while modelling Clearly Store products.

## Database ownership

The MySQL database is `clearly_store`. Catalog tables are owned by
`catalog-service`; order tables are owned by `order-service`; authentication
tables are owned by `auth-service`. The services use environment variables for
credentials and never commit passwords.

## Start order

1. MySQL
2. `discovery-service`
3. `config-server`
4. `catalog-service`, `order-service`, `notification-service`, `auth-service`
5. `gateway-service`

The browser should call only `http://localhost:8080/api/...`.

## Interactive API testing

Catalog Swagger UI is available at:

`http://localhost:8081/swagger-ui/index.html`

OpenAPI JSON is available at `http://localhost:8081/v3/api-docs`. The UI
includes product listing/detail, create/update, catalog metadata save, and
image/document upload operations.

## Customer authentication

The account page uses `auth-service` on port `8085` for password login,
signup OTP verification, JWT sessions, and Google Identity Services. Local
development displays the OTP in the verification panel. For production set
`AUTH_DEV_EXPOSE_OTP=false`, `AUTH_MAIL_ENABLED=true`, the `SMTP_*` values,
`GOOGLE_CLIENT_ID`, and a strong private `JWT_SECRET` environment value.

Auth Swagger UI is available at `http://localhost:8085/swagger-ui/index.html`.

Signed-in cart and wishlist state is stored in MySQL in `user_cart_items` and
`user_wishlist_items`. Both tables belong to a user and survive logout; browser
storage is used only as a disposable page cache and is repopulated after login.
