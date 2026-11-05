CREATE TABLE tb_tickets (
    id          UUID PRIMARY KEY,
    event_id    UUID NOT NULL,
    order_id    UUID NULL,
    price       DECIMAL(19, 2) NOT NULL,
    ticket_type VARCHAR(50) NOT NULL,
    status      VARCHAR(50) NOT NULL,

    CONSTRAINT fk_ticket_event FOREIGN KEY (event_id) REFERENCES tb_events (id),
    CONSTRAINT fk_ticket_order FOREIGN KEY (order_id) REFERENCES tb_orders (id),
    CONSTRAINT ck_ticket_status CHECK (status IN ('AVAILABLE', 'RESERVED', 'SOLD')),
    CONSTRAINT ck_ticket_status_order CHECK (
        (status = 'AVAILABLE' AND order_id IS NULL) OR
        (status <> 'AVAILABLE' AND order_id IS NOT NULL)
    )
);

CREATE INDEX idx_tickets_event_type_status ON tb_tickets (event_id, ticket_type, status);
CREATE INDEX idx_tickets_order_id ON tb_tickets (order_id);