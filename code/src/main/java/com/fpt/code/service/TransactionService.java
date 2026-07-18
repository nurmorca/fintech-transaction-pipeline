package com.fpt.code.service;

import com.fpt.code.data.Transaction;
import com.fpt.code.data.dto.TransactionRequest;

public interface TransactionService {
    public Transaction checkIfIdempotencyKeyExists(String key);
    public Transaction createTransaction(TransactionRequest request, String key);
    public Transaction getTransaction(Long id);

}
