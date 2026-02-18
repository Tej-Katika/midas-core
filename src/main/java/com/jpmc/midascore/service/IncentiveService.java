package com.jpmc.midascore.service;

import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class IncentiveService {

    private static final Logger logger = LoggerFactory.getLogger(IncentiveService.class);

    private final RestTemplate restTemplate;
    private final String incentiveApiUrl;

    public IncentiveService(
            RestTemplate restTemplate,
            @Value("${incentive.api.url:http://localhost:8080/incentive}") String incentiveApiUrl) {
        this.restTemplate = restTemplate;
        this.incentiveApiUrl = incentiveApiUrl;
    }

    /**
     * Calls the Incentive API to get the incentive amount for a transaction
     *
     * @param transaction The transaction to get incentive for
     * @return Incentive object containing the bonus amount (>= 0)
     */
    public Incentive getIncentive(Transaction transaction) {
        logger.info("Calling Incentive API for transaction: sender={}, recipient={}, amount={}",
                transaction.getSenderId(),
                transaction.getRecipientId(),
                transaction.getAmount());

        try {
            // POST request to Incentive API
            // RestTemplate automatically:
            // 1. Serializes Transaction object to JSON
            // 2. Sends HTTP POST request
            // 3. Deserializes JSON response to Incentive object
            Incentive incentive = restTemplate.postForObject(
                    incentiveApiUrl,      // URL
                    transaction,          // Request body (auto-serialized to JSON)
                    Incentive.class       // Response type (auto-deserialized from JSON)
            );

            logger.info("Received incentive: {}", incentive != null ? incentive.getAmount() : "null");

            // Return incentive or default to 0 if API fails
            return incentive != null ? incentive : new Incentive(0.0f);

        } catch (Exception e) {
            logger.error("Error calling Incentive API: {}", e.getMessage());
            // Return 0 incentive on error (fail gracefully)
            return new Incentive(0.0f);
        }
    }
}