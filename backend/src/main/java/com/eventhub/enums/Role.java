package com.eventhub.enums;

public enum Role {
    USER,
    ADMIN;


    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isUser() {
        return this == USER;
    }

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}