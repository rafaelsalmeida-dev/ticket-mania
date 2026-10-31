CREATE TABLE tb_tickets (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    price DECIMAL(19, 2) NOT NULL,
    ticket_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,

    CONSTRAINT fk_ticket_event FOREIGN KEY (event_id) REFERENCES tb_events (id)
);