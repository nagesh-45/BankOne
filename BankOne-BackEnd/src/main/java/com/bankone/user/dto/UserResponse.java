package com.bankone.user.dto;

import java.util.List;

public class UserResponse {

    private Long userId;
    private String employeeCode;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean enabled;
    private List<String> roles;

    public UserResponse() {
    }

    public UserResponse(
            Long userId,
            String employeeCode,
            String username,
            String firstName,
            String lastName,
            String email,
            Boolean enabled,
            List<String> roles
    ) {
        this.userId = userId;
        this.employeeCode = employeeCode;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.enabled = enabled;
        this.roles = roles;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
