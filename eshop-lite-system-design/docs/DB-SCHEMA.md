# Database Schema & Conventions

**Date:** 2026-03-03

---

## 1. Users (users_db)

```sql
CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'USER',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
```

## 2. Products (catalog_db)

```sql
CREATE TABLE IF NOT EXISTS products (
  id UUID PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  sku VARCHAR(64) UNIQUE NOT NULL,
  price DECIMAL(12,2) NOT NULL CHECK (price > 0),
  currency VARCHAR(3) NOT NULL DEFAULT 'INR',
  description TEXT,
  category VARCHAR(100),
  attributes JSONB,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku);
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
```

## 3. Flyway Conventions

- File names: `V<version>__<desc>.sql` (e.g., `V1__init.sql`)
- Each change is additive and idempotent where possible
- No destructive changes without a prior data migration step
