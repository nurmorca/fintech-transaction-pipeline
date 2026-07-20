package com.fpt.code.consumer;

import com.fpt.code.config.RabbitMQConfig;
import com.fpt.code.data.AuditLog;
import com.fpt.code.data.LedgerEntry;
import com.fpt.code.data.Transaction;
import com.fpt.code.data.record.TransactionInitiatedEvent;
import com.fpt.code.repository.AuditLogRepository;
import com.fpt.code.repository.LedgerEntryRepository;
import com.fpt.code.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class LedgerConsumer {

    private final RabbitTemplate rabbitTemplate;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final AuditLogRepository auditLogRepository;

    @Autowired
    public LedgerConsumer(RabbitTemplate template, TransactionRepository transactionRepo, LedgerEntryRepository ledgerRepo, AuditLogRepository auditLogRepository) {
        this.rabbitTemplate = template;
        this.transactionRepository = transactionRepo;
        this.ledgerEntryRepository = ledgerRepo;
        this.auditLogRepository = auditLogRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.FRAUD_CHECK_FAILED_QUEUE)
    @Transactional
    public void handleFraudFlaggedTransaction(TransactionInitiatedEvent event) {
        log.info("Fraud flagged transaction {} received for ledger", event.transactionId());

        Transaction transaction = transactionRepository.findById(event.transactionId()).orElseThrow();
        transaction.setStatus(Transaction.STATUS_DECLINED);
        transactionRepository.save(transaction);

        List<LedgerEntry> entries = ledgerEntryRepository.findAllByTransactionId(event.transactionId());
        if (!entries.isEmpty()) {
            LedgerEntry credit = entries.stream().filter(e -> e.getEntryType().equals(LedgerEntry.TYPE_CREDIT)).findFirst().orElseThrow();
            LedgerEntry debit = entries.stream().filter(e -> e.getEntryType().equals(LedgerEntry.TYPE_DEBIT)).findFirst().orElseThrow();

            LedgerEntry reverseCredit = new LedgerEntry();
            reverseCredit.setTransactionId(transaction.getId());
            reverseCredit.setAmount(transaction.getAmount());
            reverseCredit.setAccount(debit.getAccount());
            reverseCredit.setEntryType(LedgerEntry.TYPE_CREDIT);
            reverseCredit.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

            LedgerEntry reverseDebit = new LedgerEntry();
            reverseDebit.setTransactionId(transaction.getId());
            reverseDebit.setAmount(transaction.getAmount());
            reverseDebit.setAccount(credit.getAccount());
            reverseDebit.setEntryType(LedgerEntry.TYPE_DEBIT);
            reverseDebit.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

            ledgerEntryRepository.save(reverseDebit);
            ledgerEntryRepository.save(reverseCredit);
        }
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.TRANSACTION_DECLINED_KEY, event);
        AuditLog log = new AuditLog();
        log.setTransactionId(transaction.getId());
        log.setEvent(RabbitMQConfig.TRANSACTION_DECLINED_KEY);
        log.setDetails("Amount: " + transaction.getAmount());
        log.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        auditLogRepository.save(log);
    }

    @RabbitListener(queues = RabbitMQConfig.FRAUD_CHECK_PASSED_QUEUE)
    @Transactional
    public void handleFraudCheckPassedTransaction(TransactionInitiatedEvent event) {
        log.info("Fraud check passed transaction {} received for ledger", event.transactionId());

        Transaction transaction = transactionRepository.findById(event.transactionId()).orElseThrow();
        List<LedgerEntry> entries = ledgerEntryRepository.findAllByTransactionId(event.transactionId());
        if (entries.isEmpty() && Transaction.STATUS_FRAUD_CHECK_PASSED.equals(transaction.getStatus())) {
            LedgerEntry credit = new LedgerEntry();
            credit.setTransactionId(transaction.getId());
            credit.setAmount(transaction.getAmount());
            credit.setAccount(transaction.getReceiver());
            credit.setEntryType(LedgerEntry.TYPE_CREDIT);
            credit.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

            LedgerEntry debit = new LedgerEntry();
            debit.setTransactionId(transaction.getId());
            debit.setAmount(transaction.getAmount());
            debit.setAccount(transaction.getSender());
            debit.setEntryType(LedgerEntry.TYPE_DEBIT);
            debit.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

            transaction.setStatus(Transaction.STATUS_SETTLED);

            ledgerEntryRepository.save(debit);
            ledgerEntryRepository.save(credit);
            transactionRepository.save(transaction);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.TRANSACTION_SETTLED_KEY, event);
            AuditLog log = new AuditLog();
            log.setTransactionId(transaction.getId());
            log.setEvent(RabbitMQConfig.TRANSACTION_SETTLED_KEY);
            log.setDetails("Amount: " + transaction.getAmount());
            log.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            auditLogRepository.save(log);
        } else {
            log.info("Fraud check passed transaction {} is already settled in ledger, skipped", event.transactionId());
        }
    }
}
