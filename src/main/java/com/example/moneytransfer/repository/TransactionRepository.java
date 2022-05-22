package com.example.moneytransfer.repository;

import com.example.moneytransfer.entity.Transaction;
import com.example.moneytransfer.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    Optional<Transaction> findByCode(String code);
    Page<Transaction> findAllByUserSender(User sender, Pageable pageRequest);
    Page<Transaction> findAllByUserReceiver(User receiver, Pageable pageRequest);
    @Query(value = "SELECT sum(amount) FROM transactions WHERE date_created BETWEEN ?1 and ?2", nativeQuery = true)
    Long countByDateBetween(LocalDate date1, LocalDate date2);
}
