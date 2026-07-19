package com.fpt.code.repository;

import com.fpt.code.data.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findAllByTransactionId(Long transactionId);

}
