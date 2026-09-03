package com.banking.accountservice.dto;

import com.banking.accountservice.entity.AccountType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateAccountRequest(

        @NotBlank(message = "Account holder name is required.")
        String accountHolderName,

        @NotBlank(message = "Email is required.")
        @Email(message = "Invalid email format.")
        String email,

        @NotBlank(message = "Phone is required.")
        String phone,

        @NotNull(message = "Account type is required.")
        AccountType accountType,

        @NotNull(message = "Initial deposit is required.")
        @Positive(message = "Initial deposit must be positive.")
        BigDecimal initialDeposit


) {
}
