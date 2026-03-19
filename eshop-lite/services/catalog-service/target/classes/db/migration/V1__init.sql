-- Flyway V1
CREATE TABLE IF NOT EXISTS products (
  id UUID PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  sku VARCHAR(64) NOT NULL UNIQUE,
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
