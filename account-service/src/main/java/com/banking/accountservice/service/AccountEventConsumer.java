package com.banking.accountservice.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class AccountEventConsumer {

    private final AccountService accountService;


    /*
    * Consumes transaction.completed events from Kafka
    * Credits receiver account
    * */
    @KafkaListener(topics = "transaction.completed")
    public void consumeTransactionCompleted(
            @Payload Map<String , Object> payload
            ){

        try {

            String receiverAccount = (String) payload.get("ReceiverAccountNumber").toString();

            BigDecimal amount = new BigDecimal(payload.get("Amount").toString());

            log.info("Crediting account: {} with amount: {}", receiverAccount, amount);

            accountService.creditBalance(receiverAccount, amount);

        }
        catch (Exception e){
            log.error("Error crediting account: {}", e.getMessage());
        }

    }


    /*
    * Consumes fraud.detected events from kafka
    * Blocks the flagged account
    * */
    @KafkaListener(topics = "fraud.detected")
    public void consumeFraudDetected(
            @Payload Map<String , Object> payload
    ){

        try {

            String accountNumber = (String) payload.get("AccountNumber");

            log.info("Fraud detected - blocking account: {}", accountNumber);

            accountService.blockAccount(accountNumber);

        } catch (Exception e) {
            log.error("Error blocking account: {}", e.getMessage());
        }
    }
}
