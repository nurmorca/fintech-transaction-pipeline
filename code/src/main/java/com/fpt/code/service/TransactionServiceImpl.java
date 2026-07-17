package com.fpt.code.service;

import com.fpt.code.config.RabbitMQConfig;
import com.fpt.code.data.Transaction;
import com.fpt.code.data.TransactionInitiatedEvent;
import com.fpt.code.data.dto.TransactionRequest;
import com.fpt.code.repository.TransactionRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TransactionServiceImpl implements TransactionService {

    private TransactionRepository repo;
    private RabbitTemplate template;

    @Autowired
    public TransactionServiceImpl(TransactionRepository repo, RabbitTemplate template) {
        this.repo = repo;
        this.template = template;
    }

    @Override
    public Transaction createTransaction(TransactionRequest request, String idempotencyKey) {
        Transaction existing = checkIfIdempotencyKeyExists(idempotencyKey);
        if (existing != null) {
            return existing;
        }
        Transaction transaction = new Transaction();
        transaction.setSender(request.getSender());
        transaction.setReceiver(request.getReceiver());
        transaction.setAmount(request.getAmount());
        transaction.setIdempotencyKey(idempotencyKey);
        transaction.setStatus(Transaction.STATUS_PENDING);
        transaction.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        Transaction saved = repo.save(transaction);

        TransactionInitiatedEvent event = new TransactionInitiatedEvent(
                saved.getId(),
                saved.getIdempotencyKey(),
                saved.getSender(),
                saved.getReceiver(),
                saved.getAmount()
        );

        template.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
        return null;
    }


    @Override
    public Transaction checkIfIdempotencyKeyExists(String key) {
        Optional<Transaction> result = repo.findByIdempotencyKey(key);
        if (result.isPresent()) {
            return result.get();
        }
        return null;
    }
}
