package com.banking.accountservice.service;

import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.entity.Account;
import com.banking.accountservice.entity.AccountStatus;
import com.banking.accountservice.entity.AccountType;
import com.banking.accountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository  accountRepository;
    private static SecureRandom secureRandom = new SecureRandom();


    public AccountResponse createAccount(CreateAccountRequest request){

        log.info("Creating account for: {} ", request.email());

        if( accountRepository.existsByEmail(request.email()) ){
            throw new RuntimeException("Account already exists");
        }

        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .accountHolderName(request.accountHolderName())
                .email(request.email())
                .phone(request.phone())
                .accountType(request.accountType())
                .accountStatus(AccountStatus.ACTIVE)
                .balance(request.initialDeposit())
                .dailyTransactionLimit(request.accountType() == AccountType.SAVINGS
                        ? new BigDecimal("500000") : new BigDecimal("100000")
                )
                .build();

        Account savedAccount = accountRepository.save(account);

        log.info("Saved account for: {} ", savedAccount.getAccountNumber());

        return mapToResponse(savedAccount);

    }


    private AccountResponse mapToResponse(Account account){

        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountHolderName(),
                account.getEmail(),
                account.getPhone(),
                account.getAccountType(),
                account.getAccountStatus(),
                account.getBalance(),
                account.getDailyTransactionLimit(),
                account.getCreatedAt()
        );
    }

    public AccountResponse getAccount(String accountNumber){
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(()-> new RuntimeException("Account not found."));


        return mapToResponse(account);
    }

    public BigDecimal getBalance(String accountNumber){
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(()-> new RuntimeException("Account not found."));


        return account.getBalance();
    }

    /*
    * Generate unique 12 digit account number
    * */
    private String generateAccountNumber(){
        String accountNumber;

        do {
            long number = secureRandom.nextLong(1_000_000_000_000L);

            accountNumber = String.format("%012d", number);

        } while (accountRepository.exitsByAccountNumber(accountNumber));

        return accountNumber;



    }


}
