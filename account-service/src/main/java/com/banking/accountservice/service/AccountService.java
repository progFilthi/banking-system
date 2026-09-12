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
    private static final SecureRandom secureRandom = new SecureRandom();


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

    /*
     *     GET account by account number
    *
    *  */
    public AccountResponse getAccount(String accountNumber){
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(()-> new RuntimeException("Account not found."));


        return mapToResponse(account);
    }


    /*
    * GET account balance by account number
    * */
    public BigDecimal getBalance(String accountNumber){
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(()-> new RuntimeException("Account not found."));


        return account.getBalance();
    }


    /* Block account - Called by fraud detection service. */
    public void blockAccount(String accountNumber){
        log.info("Blocking account for: {} ", accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(()-> new RuntimeException("Account not found."));

        account.setAccountStatus(AccountStatus.BLOCKED);

        accountRepository.save(account);
    }

    /*
    * Deduct Balance from sender
    * Called by Transaction Service
    * */
    public void deductBalance(String accountNumber, BigDecimal balance){
        log.info("Deducting balance {} from accoun {} ", balance, accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(()-> new RuntimeException("Account not found."));

        if( account.getAccountStatus() != AccountStatus.ACTIVE ){
            throw new RuntimeException("Account status not active for this account: " + accountNumber);
        }

        if( account.getBalance().compareTo(balance) < 0 ){
            throw new RuntimeException("Sorry you have insufficient funds for this transaction: " + accountNumber);
        }

        account.setBalance(account.getBalance().subtract(balance));

        accountRepository.save(account);

        log.info("Balance updated. New Balance {} ", account.getBalance());

    }

    /*
    * Credit Balance
    * Called by Transaction Service via Kafka
    * */
    public void creditBalance(String accountNumber, BigDecimal balance){

        log.info("Crediting balance {} from account {} ", balance, accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(()-> new RuntimeException("Account not found."));


        account.setBalance(account.getBalance().add(balance));

        accountRepository.save(account);

        log.info("Account credited. New Balance {} ", account.getBalance());



    }

    /*
    * Generate unique 12-digit account number
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
