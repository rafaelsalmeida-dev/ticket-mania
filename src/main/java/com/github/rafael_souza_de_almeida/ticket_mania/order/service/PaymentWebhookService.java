package com.github.rafael_souza_de_almeida.ticket_mania.order.service;

import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Order;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.OrderStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.OrderRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentWebhookService {

    private final OrderRepository orderRepository;

    public PaymentWebhookService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    Logger log = LoggerFactory.getLogger(PaymentWebhookService.class);

    @Value("${stripe.secret.webhook}")
    private String webhookSecret;

    public void processWebhookEvent(String payload, String signatureHeader) {
        Event event = validateAndParseEvent(payload, signatureHeader);

        if ("payment_intent.succeeded".equals(event.getType())) {
            handlePaymentSucceeded(event);
        } else {
            log.info("Stripe event ignored: {}", event.getType());
        }
    }

    private Event validateAndParseEvent(String payload, String signatureHeader) {
        try {
            return Webhook.constructEvent(payload, signatureHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Invalid webhook signature {}", e.getMessage());
            throw new IllegalArgumentException("Invalid signature", e);
        }
    }

    private void handlePaymentSucceeded(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new IllegalStateException("Payment Intent not found"));

        String orderIdString = paymentIntent.getMetadata().get("orderId");
        if (orderIdString == null) {
            log.warn("PaymentIntent {} without order in metaData", paymentIntent.getId());
            return;
        }

        UUID orderId = UUID.fromString(orderIdString);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));

        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        log.info("Confirmed payment via order: {}", orderId);
    }

}
