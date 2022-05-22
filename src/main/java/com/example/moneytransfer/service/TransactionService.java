package com.example.moneytransfer.service;

import com.example.moneytransfer.entity.Transaction;
import com.example.moneytransfer.paging.Paged;
import com.example.moneytransfer.request.RefreshTransactionRequest;
import com.example.moneytransfer.request.SendTransactionRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TransactionService {

    Transaction send(SendTransactionRequest request);

    Transaction update(Long id, String newStatus);

    Paged<Transaction> getAll(int pageNum, int pageSize);

    Paged<Transaction> getAllBySender(String sender, int pageNum, int pageSize);

    Paged<Transaction> getAllByReceiver(String receiver, int pageNum, int pageSize);

    Transaction receive(String code);

    Transaction refresh(RefreshTransactionRequest request);
}
