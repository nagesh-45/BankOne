package com.bankone.dashboard.dto;

public class DashboardResponse {

    private long customerCount;
    private long accountCount;
    private long employeeCount;
    private long todayTransactionCount;

    public DashboardResponse() {
    }

    public DashboardResponse(long customerCount,
                             long accountCount,
                             long employeeCount,
                             long todayTransactionCount) {
        this.customerCount = customerCount;
        this.accountCount = accountCount;
        this.employeeCount = employeeCount;
        this.todayTransactionCount = todayTransactionCount;
    }

    public long getCustomerCount() {
        return customerCount;
    }

    public void setCustomerCount(long customerCount) {
        this.customerCount = customerCount;
    }

    public long getAccountCount() {
        return accountCount;
    }

    public void setAccountCount(long accountCount) {
        this.accountCount = accountCount;
    }

    public long getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(long employeeCount) {
        this.employeeCount = employeeCount;
    }

    public long getTodayTransactionCount() {
        return todayTransactionCount;
    }

    public void setTodayTransactionCount(long todayTransactionCount) {
        this.todayTransactionCount = todayTransactionCount;
    }
}