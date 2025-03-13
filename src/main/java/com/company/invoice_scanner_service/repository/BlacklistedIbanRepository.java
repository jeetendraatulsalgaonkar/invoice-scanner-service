package com.company.invoice_scanner_service.repository;

import com.company.invoice_scanner_service.entity.BlacklistedIban;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlacklistedIbanRepository extends JpaRepository<BlacklistedIban, Long> {

    /**
     * Finds blacklisted IBANs from a given list.
     */
    List<BlacklistedIban> findByIbanIn(List<String> ibans);
}
