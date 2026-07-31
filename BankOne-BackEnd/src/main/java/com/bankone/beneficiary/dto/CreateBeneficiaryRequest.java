package com.bankone.beneficiary.dto;

import com.bankone.beneficiary.enums.BeneficiaryBankType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateBeneficiaryRequest {

    @NotBlank
    @Size(max = 100)
    private String nickname;

    @NotNull
    private BeneficiaryBankType bankType;

    @NotBlank
    @Size(max = 34)
    private String accountNumber;

    @NotBlank
    @Size(max = 120)
    private String accountHolderName;

    @Size(max = 20)
    private String ifsc;

    @Size(max = 120)
    private String bankName;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public BeneficiaryBankType getBankType() {
        return bankType;
    }

    public void setBankType(BeneficiaryBankType bankType) {
        this.bankType = bankType;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public String getIfsc() {
        return ifsc;
    }

    public void setIfsc(String ifsc) {
        this.ifsc = ifsc;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
}
