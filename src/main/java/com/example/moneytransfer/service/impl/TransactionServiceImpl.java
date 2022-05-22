package com.example.moneytransfer.service.impl;

import com.example.moneytransfer.Enums.Currency;
import com.example.moneytransfer.Enums.Status;
import com.example.moneytransfer.entity.Transaction;
import com.example.moneytransfer.entity.User;
import com.example.moneytransfer.paging.Paged;
import com.example.moneytransfer.paging.Paging;
import com.example.moneytransfer.repository.ClientRepository;
import com.example.moneytransfer.repository.TransactionRepository;
import com.example.moneytransfer.repository.UserRepository;
import com.example.moneytransfer.request.RefreshTransactionRequest;
import com.example.moneytransfer.request.SendTransactionRequest;
import com.example.moneytransfer.service.TransactionService;
import com.example.moneytransfer.utils.CodeGenerator;
import com.example.moneytransfer.utils.DateConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
/*@EnableScheduling*/
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  UserRepository userRepository,
                                  ClientRepository clientRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
    }

    @Override
    public Transaction send(SendTransactionRequest request) {
        if (request.getUsernameReceiver().equals(request.getUsernameSender())) {
            throw new RuntimeException("Не допустимая транзакция");
        }
        User userReceiver = userRepository.findByUsername(request.getUsernameReceiver())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        User userSender = userRepository.findByUsername(request.getUsernameSender())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!checkClientUser(request.getPhoneNumberReceiver(), userReceiver))
        {
            throw new RuntimeException("Номер не принадлежит пользователю");
        } else if (!checkClientUser(request.getPhoneNumberSender(), userSender))
        {
            throw new RuntimeException("Номер не принадлежит пользователю");
        }

        Transaction transaction = Transaction.builder()
                .userSender(userSender)
                .userReceiver(userReceiver)
                .senderClientNumber(request.getPhoneNumberSender())
                .receiverClientNumber(request.getPhoneNumberReceiver())
                .description(request.getDescription())
                .currency(Currency.valueOf(request.getCurrency()))
                .status(Status.ACTIVE)
                .amount(request.getAmount())
                .code(CodeGenerator.generate(10))
                .build();
        return transactionRepository.save(transaction);
    }

    public Boolean checkClientUser(String phoneNumber, User user) {
        return user.getClients().stream()
                .map(client1 -> client1.getPhoneNumber().contains(phoneNumber))
                .findAny().orElseThrow(() -> new RuntimeException("номер не принадлежит пользователю"));
    }

    @Override
    public Transaction update(Long id, String newStatus) {
        return transactionRepository.findById(id)
                .map(transaction1 -> {
                    transaction1.setStatus(Status.valueOf(newStatus));
                    transactionRepository.save(transaction1);
                    return transaction1;
                }).orElseThrow(() -> new RuntimeException("transaction not found exception"));
    }

    @Override
    public Paged<Transaction> getAll(int pageNum, int pageSize, String sortField, String sortDir) {
        PageRequest request = PageRequest.of(pageNum - 1, pageSize, sortDir.equals("asc") ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending());
        Page<Transaction> postPage = transactionRepository.findAll(request);

        return new Paged<>(postPage, Paging.of(postPage.getTotalPages(), pageNum, pageSize));
    }

    @Override
    public Paged<Transaction> getAllBySender(String sender, int pageNum, int pageSize, String sortField, String sortDir) {
        User sender1 = userRepository.findByUsername(sender)
                .orElseThrow(() -> new RuntimeException());

        PageRequest request = PageRequest.of(pageNum - 1, pageSize, sortDir.equals("asc") ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending());
        Page<Transaction> postPage = transactionRepository.findAllByUserSender(sender1, request);
        return new Paged<>(postPage, Paging.of(postPage.getTotalPages(), pageNum, pageSize));
    }

    @Override
    public Paged<Transaction> getAllByReceiver(String receiver, int pageNum, int pageSize, String sortField, String sortDir) {
        User receiver1 = userRepository.findByUsername(receiver)
                .orElseThrow(() -> new RuntimeException());

        PageRequest request = PageRequest.of(pageNum - 1, pageSize, sortDir.equals("asc") ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending());
        Page<Transaction> postPage = transactionRepository.findAllByUserReceiver(receiver1, request);
        return new Paged<>(postPage, Paging.of(postPage.getTotalPages(), pageNum, pageSize));
    }

    @Override
    public Transaction receive(String code) {
        return transactionRepository.findByCode(code)
                .map(transaction1 -> {
                    transaction1.setStatus(Status.COMPLETE);
                    return transactionRepository.save(transaction1);
                })
                .orElseThrow(() -> new RuntimeException("Такой транзакции не существует"));
    }

    @Override
    public Transaction refresh(RefreshTransactionRequest request) {
        return transactionRepository.findByCode(request.getOldCode())
                .map(transaction1 -> {
                    transaction1.setStatus(Status.ACTIVE);
                    transaction1.setDateCreated(DateConverter.convertToDateViaInstant(LocalDateTime.now()));
                    transaction1.setCode(CodeGenerator.generate(10));
                    return transactionRepository.save(transaction1);
                })
                .orElseThrow(() -> new RuntimeException("Такой транзакции не существует"));
    }

    @Override
    public Long getStatistics(LocalDate date1, LocalDate date2) {
        return transactionRepository.countByDateBetween(date1, date2);
    }

    @Scheduled(cron = "0 * * * * *")
    public void checkExpiration() {
        List<Transaction> allTransactions = transactionRepository.findAll();
        for (Transaction transaction : allTransactions) {
            if (transaction.getDateCreated().before(DateConverter.convertToDateViaInstant(LocalDateTime.now().minusMinutes(10)))
                    && transaction.getStatus().equals(Status.valueOf("ACTIVE"))) {
                transaction.setStatus(Status.OVERDUE);
                transactionRepository.save(transaction);
            }
        }
    }

}
