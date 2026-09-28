# Project structure

Clearly is a static storefront backed by independent Spring Boot services. The complete browser application lives under `frontend/`, keeping the repository root focused on major project areas.

```text
clearly/
├── frontend/                  # Complete storefront application
│   ├── admin/                 # Catalogue and storefront administration UI
│   ├── assets/                # Product, category, brand and homepage media
│   ├── data/                  # Development catalogue fallback data
│   ├── *.html                 # Storefront pages
│   ├── *.css                  # Page and shared styles
│   ├── *.js                   # Browser modules
│   └── server.js              # Local preview server
├── backend/                   # Spring Boot microservices
│   ├── auth-service/          # Accounts, JWT, checkout and reviews
│   ├── catalog-service/       # Products, categories, packages and uploads
│   ├── config-server/         # Central configuration service
│   ├── discovery-service/     # Eureka service registry
│   ├── gateway-service/       # Public API gateway
│   ├── notification-service/  # Email and SMS order notifications
│   └── order-service/         # Order lifecycle and events
├── docs/                      # Architecture and contributor documentation
│   ├── README.md              # Product overview and local setup
│   ├── CONTRIBUTING.md        # Contribution guidelines
│   └── PROJECT_STRUCTURE.md   # Repository conventions
├── config/                    # Safe environment-variable templates
└── .gitignore                 # Repository-wide safety exclusions
```

## Frontend conventions

- Shared navigation and footer: `frontend/site-nav.js`, `frontend/site-nav.css`, `frontend/site-nav-menu.css`
- Shared shopping state: `frontend/shop-state.js`
- Live catalogue adapter: `frontend/catalog-live.js`
- Shared product cards: `frontend/product-card-shared.css`
- Page-specific files use the same base name, for example `contact.html`, `contact.css`, and `contact.js`.
- Images belong under `frontend/assets/`; do not add image files to the repository root.
- Run the local preview from `frontend/` so public URLs such as `/products.html` remain unchanged.

## Backend conventions

Each service owns its Java source, resources, Maven configuration, and tests. Runtime credentials must be supplied through environment variables. Do not commit keys, tokens, passwords, build output, logs, or uploaded test files.

## Files excluded from Git

The repository ignores Maven targets, local dependency caches, runtime logs, preview output, temporary work directories, IDE metadata, and local environment files. Use `.env.example` as the list of supported settings.

## Legacy-file policy

Some older browser scripts remain under `frontend/` while the active storefront references newer replacements. Do not delete a legacy file solely because it is not referenced by one page; first search the full repository and verify deployed routes. New work should use `frontend/products-app.v2.js` rather than adding another versioned copy.
