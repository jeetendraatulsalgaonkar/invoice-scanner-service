SET search_path TO "iban_compliance";

CREATE TABLE IF NOT EXISTS "iban_compliance"."blacklisted_ibans" (
    id SERIAL PRIMARY KEY,
    iban VARCHAR(34) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

GRANT ALL PRIVILEGES ON TABLE "iban_compliance"."blacklisted_ibans" TO postgres;
