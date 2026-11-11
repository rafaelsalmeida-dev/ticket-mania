package com.github.rafael_souza_de_almeida.ticket_mania.order.consumer;

import com.github.rafael_souza_de_almeida.ticket_mania.core.rabbitMq.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderTicketConsumer {

    Logger log = LoggerFactory.getLogger(OrderTicketConsumer.class);

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumeOrderPaidEvent(String orderId) {

      log.info("New message detected!");
      log.info("Init order processing {}", orderId);

        try {
            Thread.sleep(2000);
            System.out.println("PDF generated successfully! Sent ticket to the client's email");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }

}
