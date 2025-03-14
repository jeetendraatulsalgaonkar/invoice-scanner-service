SET search_path TO "iban_compliance";

ALTER TABLE "iban_compliance"."blacklisted_ibans" ADD COLUMN reason TEXT NOT NULL DEFAULT 'DEFAULT';