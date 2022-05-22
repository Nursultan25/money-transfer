package com.example.moneytransfer.repository;

import com.example.moneytransfer.entity.Transaction;
import com.example.moneytransfer.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByCode(String code);
    Page<Transaction> findAllByUserSenderOrderByDateCreatedDesc(User sender, Pageable pageRequest);
    Page<Transaction> findAllByUserReceiverOrderByDateCreatedDesc(User receiver, Pageable pageRequest);
}
