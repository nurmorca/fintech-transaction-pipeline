package com.fpt.code.data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "transactions")
@Entity
public class Transaction {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_FRAUD_FLAGGED = "FRAUD_FLAGGED";
    public static final String STATUS_FRAUD_CHECK_PASSED = "FRAUD_CHECK_PASSED";
    public static final String STATUS_DECLINED = "DECLINED";
    public static final String STATUS_SETTLED = "SETTLED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String idempotencyKey;
    private String sender;
    private String receiver;
    @Column(precision = 19, scale = 4, nullable = false) //doing this because precision and scale are important in numbers.
    private BigDecimal amount;
    @Column(nullable = false)
    private String status;
    @Column(updatable = false, nullable = false)
    private Timestamp createdAt;

    @OneToMany(mappedBy = "transaction", fetch = FetchType.LAZY)
    private List<LedgerEntry> entries;

}
