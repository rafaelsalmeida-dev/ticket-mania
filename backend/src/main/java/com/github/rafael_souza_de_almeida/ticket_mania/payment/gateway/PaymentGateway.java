package com.github.rafael_souza_de_almeida.ticket_mania.payment.gateway;

import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Order;
import com.github.rafael_souza_de_almeida.ticket_mania.payment.dto.PaymentInit;

public interface PaymentGateway {


    PaymentInit start(Order order);

    void cancel(Order order);
}
