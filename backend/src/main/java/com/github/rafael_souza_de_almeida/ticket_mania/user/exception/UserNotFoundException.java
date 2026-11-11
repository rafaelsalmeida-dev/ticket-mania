package com.github.rafael_souza_de_almeida.ticket_mania.user.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String s) {
        super(s);
    }
}
