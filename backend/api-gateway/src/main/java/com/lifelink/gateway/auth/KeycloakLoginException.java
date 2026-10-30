package com.lifelink.gateway.auth;

public class KeycloakLoginException extends RuntimeException {

    public KeycloakLoginException(String message) {
        super(message);
    }
}
