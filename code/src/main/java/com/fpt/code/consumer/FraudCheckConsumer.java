package com.fpt.code.consumer;

import com.fpt.code.config.RabbitMQConfig;
import com.fpt.code.data.Transaction;
import com.fpt.code.data.TransactionInitiatedEvent;
import com.fpt.code.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class FraudCheckConsumer {

    private final RabbitTemplate rabbitTemplate;
    private final TransactionRepository transactionRepository;

    @Autowired
    public FraudCheckConsumer(RabbitTemplate template, TransactionRepository repo) {
        this.rabbitTemplate = template;
        this.transactionRepository = repo;
    }

    @RabbitListener(queues = RabbitMQConfig.TRANSACTION_INITIATED_QUEUE)
    public void handleTransactionInitiated(TransactionInitiatedEvent event) {
        log.info("Fraud check received for transaction {}", event.transactionId());

        boolean flagged = event.amount().compareTo(new BigDecimal("10000")) > 0;

        Transaction transaction = transactionRepository.findById(event.transactionId()).orElseThrow();

        if (flagged) {
            transaction.setStatus(Transaction.STATUS_FRAUD_FLAGGED);
            transactionRepository.save(transaction);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.FRAUD_CHECK_FAILED_KEY, event);
        } else {
            transaction.setStatus(Transaction.STATUS_FRAUD_CHECK_PASSED);
            transactionRepository.save(transaction);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.FRAUD_CHECK_PASSED_KEY, event);
        }
    }
}
