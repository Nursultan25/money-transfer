package com.example.moneytransfer.service;

import com.example.moneytransfer.entity.Transaction;
import com.example.moneytransfer.request.SendTransactionRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TransactionService {

    Transaction send(SendTransactionRequest request);

    Transaction update(Long id, String newStatus);

    List<Transaction> getAll();

    List<Transaction> getAllBySender(String sender);

    List<Transaction> getAllByReceiver(String receiver);

    Transaction receive(String code);
}
