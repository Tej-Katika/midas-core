package com.jpmc.midascore.service;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.User;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRecordRepository;
    private final IncentiveService incentiveService;  // NEW DEPENDENCY

    public TransactionService(
            UserRepository userRepository,
            TransactionRecordRepository transactionRecordRepository,
            IncentiveService incentiveService) {  // INJECTED
        this.userRepository = userRepository;
        this.transactionRecordRepository = transactionRecordRepository;
        this.incentiveService = incentiveService;
    }

    @Transactional
    public void processTransaction(Transaction transaction) {
        logger.info("Processing transaction: sender={}, recipient={}, amount={}",
                transaction.getSenderId(),
                transaction.getRecipientId(),
                transaction.getAmount());

        // Step 1: Validate sender exists
        Optional<User> senderOpt = userRepository.findById(transaction.getSenderId());
        if (senderOpt.isEmpty()) {
            logger.warn("Transaction rejected: Sender ID {} not found",
                    transaction.getSenderId());
            return;
        }

        // Step 2: Validate recipient exists
        Optional<User> recipientOpt = userRepository.findById(transaction.getRecipientId());
        if (recipientOpt.isEmpty()) {
            logger.warn("Transaction rejected: Recipient ID {} not found",
                    transaction.getRecipientId());
            return;
        }

        User sender = senderOpt.get();
        User recipient = recipientOpt.get();

        // Step 3: Validate sufficient balance
        if (sender.getBalance() < transaction.getAmount()) {
            logger.warn("Transaction rejected: Insufficient balance. Sender={}, Balance={}, Amount={}",
                    sender.getUserId(),
                    sender.getBalance(),
                    transaction.getAmount());
            return;
        }

        // Step 4: All validations passed - get incentive from API
        logger.info("Transaction valid - calling Incentive API");
        Incentive incentive = incentiveService.getIncentive(transaction);
        Float incentiveAmount = incentive.getAmount();

        logger.info("Incentive received: {}", incentiveAmount);

        // Step 5: Update balances
        // Sender: deduct transaction amount ONLY (not incentive)
        sender.setBalance(sender.getBalance() - transaction.getAmount());

        // Recipient: add transaction amount AND incentive
        recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentiveAmount);

        // Step 6: Create transaction record with incentive
        TransactionRecord record = new TransactionRecord(
                sender,
                recipient,
                transaction.getAmount(),
                incentiveAmount  // NEW PARAMETER
        );

        // Step 7: Save all changes (atomic due to @Transactional)
        userRepository.save(sender);
        userRepository.save(recipient);
        transactionRecordRepository.save(record);

        logger.info("Transaction processed successfully. " +
                        "Sender balance: {}, " +
                        "Recipient balance: {} (includes {} incentive)",
                sender.getBalance(),
                recipient.getBalance(),
                incentiveAmount);
    }

    @Transactional(readOnly = true)
    public void logAllBalances() {
        List<User> users = userRepository.findAll();
        logger.info("============================================================");
        logger.info("FINAL USER BALANCES:");
        logger.info("============================================================");

        for (User user : users) {
            logger.info("User: {} (ID: {}) | Balance: {}",
                    user.getUsername(),
                    user.getUserId(),
                    user.getBalance());

            // Special handling for wilbur - calculate rounded down value
            if ("wilbur".equalsIgnoreCase(user.getUsername())) {
                int roundedDown = (int) Math.floor(user.getBalance());
                logger.info(">>> WILBUR BALANCE: {} | ROUNDED DOWN: {} <<<",
                        user.getBalance(),
                        roundedDown);
                logger.info(">>> SUBMIT THIS VALUE: {} <<<", roundedDown);
            }
        }

        logger.info("============================================================");
        logger.info("Total users: {}", users.size());
        logger.info("Total transactions: {}", transactionRecordRepository.count());
        logger.info("============================================================");
    }
}