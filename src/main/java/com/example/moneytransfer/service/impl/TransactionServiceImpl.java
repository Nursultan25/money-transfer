package com.example.moneytransfer.service.impl;

import com.example.moneytransfer.Enums.Currency;
import com.example.moneytransfer.Enums.Status;
import com.example.moneytransfer.entity.Transaction;
import com.example.moneytransfer.entity.User;
import com.example.moneytransfer.repository.ClientRepository;
import com.example.moneytransfer.repository.TransactionRepository;
import com.example.moneytransfer.repository.UserRepository;
import com.example.moneytransfer.request.SendTransactionRequest;
import com.example.moneytransfer.service.TransactionService;
import com.example.moneytransfer.utils.CodeGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
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
    public List<Transaction> getAll() {
        return transactionRepository.findAll();
    }

    @Override
    public List<Transaction> getAllBySender(String sender) {
        User sender1 = userRepository.findByUsername(sender)
                .orElseThrow(() -> new RuntimeException());
        return transactionRepository.findAllByUserSenderOrderByDateCreatedDesc(sender1);
    }

    @Override
    public List<Transaction> getAllByReceiver(String receiver) {
        User receiver1 = userRepository.findByUsername(receiver)
                .orElseThrow(() -> new RuntimeException());
        return transactionRepository.findAllByUserReceiverOrderByDateCreatedDesc(receiver1);
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

}
