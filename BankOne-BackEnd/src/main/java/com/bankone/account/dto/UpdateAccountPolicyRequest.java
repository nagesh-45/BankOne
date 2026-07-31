package com.bankone.account.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UpdateAccountPolicyRequest {

    @NotNull
    private Boolean openingDepositRequired;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal requiredOpeningDeposit;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal minimumBalance;

    @NotNull
    private Boolean active;

    @NotNull
    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveTo;
}
