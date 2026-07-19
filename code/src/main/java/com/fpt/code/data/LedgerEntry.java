package com.fpt.code.data;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    public static final String TYPE_CREDIT = "CREDIT";
    public static final String TYPE_DEBIT = "DEBIT";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String account;
    @Column(precision = 19, scale = 4, nullable = false) //doing this because precision and scale are important in numbers.
    private BigDecimal amount;
    @Column(nullable = false)
    private String entryType;
    @Column(updatable = false, nullable = false)
    private Timestamp createdAt;
    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
    private Transaction transaction;

}
