CREATE TABLE tb_orders (
    id           UUID PRIMARY KEY,
    user_id      UUID NOT NULL,
    status       VARCHAR(50) NOT NULL,
    total_amount DECIMAL(19, 2) NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES tb_users (id),
    CONSTRAINT ck_order_status CHECK (status IN ('PENDING', 'PAID', 'EXPIRED', 'CANCELED'))
);