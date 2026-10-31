CREATE TABLE tb_orders (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    ticket_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_order_ticket UNIQUE (ticket_id),

    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES tb_users (id),
    CONSTRAINT fk_order_ticket FOREIGN KEY (ticket_id) REFERENCES tb_tickets (id)
);