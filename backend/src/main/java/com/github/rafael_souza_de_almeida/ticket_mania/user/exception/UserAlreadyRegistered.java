package com.github.rafael_souza_de_almeida.ticket_mania.user.exception;

public class UserAlreadyRegistered extends RuntimeException {
    public UserAlreadyRegistered(String s) {
        super(s);
    }
}
