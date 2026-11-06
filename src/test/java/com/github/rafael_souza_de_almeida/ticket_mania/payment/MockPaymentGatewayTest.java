package com.github.rafael_souza_de_almeida.ticket_mania.payment;

import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Order;
import com.github.rafael_souza_de_almeida.ticket_mania.payment.impl.MockPaymentGateway;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MockPaymentGatewayTest {

    @Test
    void startReturnsPaymentReferenceForThePersistedOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);

        var result = new MockPaymentGateway().start(order);

        assertEquals("MOCK", result.provider());
        assertEquals("mock_" + orderId, result.reference());
    }

    @Test
    void cancelIsSafeForMockPayments() {
        new MockPaymentGateway().cancel(new Order());
    }
}
