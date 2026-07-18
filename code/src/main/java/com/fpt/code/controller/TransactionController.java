package com.fpt.code.controller;

import com.fpt.code.data.Transaction;
import com.fpt.code.data.dto.TransactionRequest;
import com.fpt.code.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("api")
public class TransactionController {

    private TransactionService service;

    @Autowired
    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable("id") Long id) {
        Transaction transaction = service.getTransaction(id);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/transaction")
    public ResponseEntity<Object> createTransaction(@RequestBody TransactionRequest request, @RequestHeader("Idempotency-Key") String idempotencyKey) {
        Transaction transaction = service.createTransaction(request, idempotencyKey);
        if (transaction != null) {
            return ResponseEntity.ok(transaction);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("Transaction created");
    }
}
