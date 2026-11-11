ALTER TABLE tb_events ADD COLUMN image_url VARCHAR(500);

-- Rock in Rio (Palco Mundo)
UPDATE tb_events
SET image_url = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b4/Rock_in_Rio_-_Madrid_2012.jpg/960px-Rock_in_Rio_-_Madrid_2012.jpg?utm_source=pt.wikipedia.org&utm_campaign=index&utm_content=thumbnail'
WHERE id = 'b3b647f1-7975-4d0f-8c7c-0a2d5e2a2c6d';

-- Lollapalooza (Visão geral do festival)
UPDATE tb_events
SET image_url = 'http://thumb.wikimedia.org/wikipedia/commons/thumb/a/a6/Miley_Cyrus_no_palco_do_Lollapalooza_Brasil_2022.jpg/500px-Miley_Cyrus_no_palco_do_Lollapalooza_Brasil_2022.jpg?utm_source=pt.wikipedia.org&utm_campaign=index&utm_content=thumbnail'
WHERE id = 'f40c741e-399a-4c28-98e3-05b1c7eb16a5';

-- CCXP (Painel / Público)
UPDATE tb_events
SET image_url = 'https://ccxp.com.br/_next/image/?url=https%3A%2F%2Fccxp-production-files.s3.us-east-1.amazonaws.com%2Fthunderpass_home_pin2_Dnovo_IMAGEM_ILUSTRATIVA_0a6f072771.png&w=2048&q=95'
WHERE id = 'd27b9c64-46ab-48d6-95a2-3b24f5a34e07';

-- BGS - Brasil Game Show (Estandes e Público)
UPDATE tb_events
SET image_url = 'https://admin.cnnbrasil.com.br/wp-content/uploads/sites/12/2024/10/BGS-e1728658268851.png?w=1200&h=900&crop=0&quality=90'
WHERE id = 'e9812a67-d817-48f5-b73a-44c1d76378f4';

-- Formula 1 GP Interlagos (Pista)
UPDATE tb_events
SET image_url = 'https://admin.cnnbrasil.com.br/wp-content/uploads/sites/12/2026/11/GP-de-Sao-Paulo-e1793711960310.jpeg?w=1200&h=900&crop=0&quality=90'
WHERE id = '5c33842c-29a5-4811-9a74-bc4839f972b2';

-- Tardezinha (Roda de Samba / Pagode / Multidão)
UPDATE tb_events
SET image_url = 'https://s2-oglobo.glbimg.com/6vo4L3ucX5wP-fpwEiQHexDoU2A=/0x0:1600x1067/888x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_da025474c0c44edd99332dddb09cabe8/internal_photos/bs/2023/M/h/SGBt4gQFiEVNV8p5C6SQ/foto320.jpg'
WHERE id = '9e4f5a3b-2d7c-4861-a472-8f9215b3c5a6';