package com.quickbite.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WalletBalanceResponse {
    private Long customerId;
    private Double balance;
}
