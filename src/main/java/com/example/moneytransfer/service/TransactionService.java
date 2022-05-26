package com.example.moneytransfer.service;

import com.example.moneytransfer.entity.Transaction;
import com.example.moneytransfer.paging.Paged;
import com.example.moneytransfer.request.RefreshTransactionRequest;
import com.example.moneytransfer.request.SendTransactionRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public interface TransactionService {

    Transaction send(SendTransactionRequest request);

    Transaction update(Long id, String newStatus);

    Paged<Transaction> getAll(int pageNum, int pageSize, String sortField, String sortDir);

    Paged<Transaction> getAllBySender(String sender, int pageNum, int pageSize, String sortField, String sortDir, Date date1, Date date2);

    Paged<Transaction> getAllByReceiver(String receiver, int pageNum, int pageSize, String sortField, String sortDir);

    Transaction receive(String code);

    Transaction refresh(RefreshTransactionRequest request);

    List<Transaction> getStatistics(String sender, Date date1, Date date2);

    Map<String, Long> calcTotalAmount(List<Transaction> transactions);
}
