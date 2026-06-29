package com.quickbite.payment.service;

import com.quickbite.payment.dto.AddMoneyRequest;
import com.quickbite.payment.dto.WalletBalanceResponse;
import com.quickbite.payment.dto.WalletPaymentRequest;
import com.quickbite.payment.entity.Wallet;
import com.quickbite.payment.entity.WalletStatement;
import com.quickbite.payment.exception.CustomException;
import com.quickbite.payment.repository.WalletRepository;
import com.quickbite.payment.repository.WalletStatementRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletStatementRepository statementRepository;

    @Transactional
    public WalletBalanceResponse addMoney(AddMoneyRequest request) {
        log.info("Adding money to wallet: customerId={}, amount={}", request.getCustomerId(), request.getAmount());

        if (request.getAmount() <= 0) {
            throw new CustomException("Amount must be positive");
        }

        Wallet wallet = walletRepository.findByCustomerId(request.getCustomerId())
                .orElseGet(() -> {
                    Wallet newWallet = new Wallet();
                    newWallet.setCustomerId(request.getCustomerId());
                    newWallet.setBalance(0.0);
                    return newWallet;
                });

        wallet.setBalance(wallet.getBalance() + request.getAmount());
        walletRepository.save(wallet);

        // Record statement
        WalletStatement statement = new WalletStatement();
        statement.setCustomerId(request.getCustomerId());
        statement.setAmount(request.getAmount());
        statement.setType(WalletStatement.TransactionType.CREDIT);
        statement.setDescription(request.getDescription() != null ? request.getDescription() : "Added money to wallet");
        statement.setReferenceId("WALLET_TOPUP");
        statement.setCreatedAt(LocalDateTime.now());
        statementRepository.save(statement);

        log.info("Money added successfully. New balance: {}", wallet.getBalance());
        return new WalletBalanceResponse(request.getCustomerId(), wallet.getBalance());
    }

    @Transactional
    public void deductFromWallet(Long customerId, Double amount, String description, String referenceId) {
        Wallet wallet = walletRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CustomException("Wallet not found for customer"));

        if (wallet.getBalance() < amount) {
            throw new CustomException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance() - amount);
        walletRepository.save(wallet);

        // Record statement
        WalletStatement statement = new WalletStatement();
        statement.setCustomerId(customerId);
        statement.setAmount(amount);
        statement.setType(WalletStatement.TransactionType.DEBIT);
        statement.setDescription(description);
        statement.setReferenceId(referenceId);
        statement.setCreatedAt(LocalDateTime.now());
        statementRepository.save(statement);

        log.info("Deducted {} from wallet. New balance: {}", amount, wallet.getBalance());
    }

    @Transactional
    public void addToWallet(Long customerId, Double amount, String description, String referenceId) {
        Wallet wallet = walletRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    Wallet newWallet = new Wallet();
                    newWallet.setCustomerId(customerId);
                    newWallet.setBalance(0.0);
                    return newWallet;
                });

        wallet.setBalance(wallet.getBalance() + amount);
        walletRepository.save(wallet);

        WalletStatement statement = new WalletStatement();
        statement.setCustomerId(customerId);
        statement.setAmount(amount);
        statement.setType(WalletStatement.TransactionType.CREDIT);
        statement.setDescription(description);
        statement.setReferenceId(referenceId);
        statement.setCreatedAt(LocalDateTime.now());
        statementRepository.save(statement);

        log.info("Added {} to wallet. New balance: {}", amount, wallet.getBalance());
    }

    public WalletBalanceResponse getBalance(Long customerId) {
        Wallet wallet = walletRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    Wallet newWallet = new Wallet();
                    newWallet.setCustomerId(customerId);
                    newWallet.setBalance(0.0);
                    return newWallet;
                });
        return new WalletBalanceResponse(customerId, wallet.getBalance());
    }

    public List<WalletStatement> getStatement(Long customerId) {
        return statementRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }
}
