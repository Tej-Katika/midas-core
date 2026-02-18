package com.jpmc.midascore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_record")
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id", referencedColumnName = "user_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recipient_id", referencedColumnName = "user_id", nullable = false)
    private User recipient;

    @Column(name = "amount", nullable = false)
    private Float amount;

    @Column(name = "incentive", nullable = false)  // NEW FIELD!
    private Float incentive;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    // Default constructor
    public TransactionRecord() {
    }

    // Updated constructor with incentive
    public TransactionRecord(User sender, User recipient, Float amount, Float incentive) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
        this.incentive = incentive;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public Float getIncentive() {  // NEW GETTER
        return incentive;
    }

    public void setIncentive(Float incentive) {  // NEW SETTER
        this.incentive = incentive;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "TransactionRecord{" +
                "transactionId=" + transactionId +
                ", senderId=" + (sender != null ? sender.getUserId() : null) +
                ", recipientId=" + (recipient != null ? recipient.getUserId() : null) +
                ", amount=" + amount +
                ", incentive=" + incentive +  // NEW IN toString
                ", timestamp=" + timestamp +
                '}';
    }
}