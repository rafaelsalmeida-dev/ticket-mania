CREATE TABLE tb_events (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    "date" TIMESTAMP NOT NULL,
    place VARCHAR(255) NOT NULL,
    capacity INTEGER NOT NULL
);