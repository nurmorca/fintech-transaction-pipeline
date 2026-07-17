CREATE TABLE IF NOT EXISTS transactions (
     id BIGSERIAL PRIMARY KEY,
     idempotency_key VARCHAR(255) UNIQUE NOT NULL,
     sender VARCHAR(255) NOT NULL,
     receiver VARCHAR(255) NOT NULL,
     amount NUMERIC(19, 4) NOT NULL,
     status VARCHAR(50) NOT NULL,
     created_at TIMESTAMP NOT NULL DEFAULT now()
);