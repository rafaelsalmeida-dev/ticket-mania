package com.github.rafael_souza_de_almeida.ticket_mania.payment.impl;

import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Order;
import com.github.rafael_souza_de_almeida.ticket_mania.payment.dto.PaymentInit;
import com.github.rafael_souza_de_almeida.ticket_mania.payment.gateway.PaymentGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.payment.provider", havingValue = "mock", matchIfMissing = true)
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public PaymentInit start(Order order) {
        return new PaymentInit("MOCK", "mock_" + order.getId());
    }

    @Override
    public void cancel(Order order) {

    }
}
