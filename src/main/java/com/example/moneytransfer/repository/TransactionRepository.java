package com.example.moneytransfer.repository;

import com.example.moneytransfer.entity.Transaction;
import com.example.moneytransfer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByCode(String code);
    List<Transaction> findAllByUserSenderOrderByDateCreatedDesc(User sender);
    List<Transaction> findAllByUserReceiverOrderByDateCreatedDesc(User receiver);
}
