package com.company.invoice_scanner_service.repository;

import com.company.invoice_scanner_service.entity.BlacklistedIban;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlacklistedIbanRepository extends JpaRepository<BlacklistedIban, Long> {

    /**
     * Finds blacklisted IBANs from a given list.
     */
    List<BlacklistedIban> findByIbanIn(List<String> ibans);

    /**
     * Checks if the iban is already blacklisted.
     * @param iban IBAN string to check if it exists
     * @return boolean value representing its existence.
     */
    boolean existsByIban(String iban);
}
