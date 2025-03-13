-- CREATE DATABASE invoice_scanner;
CREATE SCHEMA IF NOT EXISTS iban_compliance;

GRANT ALL PRIVILEGES ON DATABASE invoice_scanner TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA iban_compliance TO postgres;