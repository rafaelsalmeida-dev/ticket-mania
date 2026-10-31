INSERT INTO tb_users (id, full_name, email, password, role)
VALUES (
    gen_random_uuid(),
    'System Administrator',
    'admin@ticketmania.com',
    '$2a$10$Ox63J5tpFBTKStqofq6KweXNdMRaCuqNlhT1xwIr0L9oaGOK2K11y',
    'ROLE_ADMIN'
)
ON CONFLICT (email) DO NOTHING;