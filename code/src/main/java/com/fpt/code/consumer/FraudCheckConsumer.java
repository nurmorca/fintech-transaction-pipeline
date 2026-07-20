package com.fpt.code.consumer;

import com.fpt.code.config.RabbitMQConfig;
import com.fpt.code.data.AuditLog;
import com.fpt.code.data.Transaction;
import com.fpt.code.data.record.TransactionInitiatedEvent;
import com.fpt.code.repository.AuditLogRepository;
import com.fpt.code.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
@Slf4j
public class FraudCheckConsumer {

    private final RabbitTemplate rabbitTemplate;
    private final TransactionRepository transactionRepository;
    private final AuditLogRepository auditLogRepository;

    @Autowired
    public FraudCheckConsumer(RabbitTemplate template, TransactionRepository repo, AuditLogRepository auditLogRepository) {
        this.rabbitTemplate = template;
        this.transactionRepository = repo;
        this.auditLogRepository = auditLogRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.TRANSACTION_INITIATED_QUEUE)
    public void handleTransactionInitiated(TransactionInitiatedEvent event) {
        log.info("Fraud check received for transaction {}", event.transactionId());

        boolean flagged = event.amount().compareTo(new BigDecimal("10000")) > 0; // mocking this for demo purposes

        Transaction transaction = transactionRepository.findById(event.transactionId()).orElseThrow();

        if (flagged) {
            transaction.setStatus(Transaction.STATUS_FRAUD_FLAGGED);
            transactionRepository.save(transaction);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.FRAUD_CHECK_FAILED_KEY, event);
            AuditLog log = new AuditLog();
            log.setTransactionId(transaction.getId());
            log.setEvent(Transaction.STATUS_FRAUD_FLAGGED);
            log.setDetails("Amount: " + transaction.getAmount());
            log.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            auditLogRepository.save(log);
        } else {
            transaction.setStatus(Transaction.STATUS_FRAUD_CHECK_PASSED);
            transactionRepository.save(transaction);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.FRAUD_CHECK_PASSED_KEY, event);
            AuditLog log = new AuditLog();
            log.setTransactionId(transaction.getId());
            log.setEvent(Transaction.STATUS_FRAUD_CHECK_PASSED);
            log.setDetails("Amount: " + transaction.getAmount());
            log.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            auditLogRepository.save(log);
        }
    }
}
