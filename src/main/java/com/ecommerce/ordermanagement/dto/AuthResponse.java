package com.ecommerce.ordermanagement.dto;

public class AuthResponse {

    private String message;
    private String token;
    private String tokenType;
    private String role;

    public AuthResponse() {
    }

    public AuthResponse(
            String message,
            String token,
            String tokenType,
            String role
    ) {
        this.message = message;
        this.token = token;
        this.tokenType = tokenType;
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}