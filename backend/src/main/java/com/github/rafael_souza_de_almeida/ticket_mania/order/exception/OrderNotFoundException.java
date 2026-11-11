package com.github.rafael_souza_de_almeida.ticket_mania.order.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String s) {
        super(s);
    }
}
