CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO municipality (name, state) VALUES
('São Paulo', 'SP'),
('Campinas', 'SP'),
('Rio de Janeiro', 'RJ');

INSERT INTO user_model (name, email, cpf, phone, password, municipality_id)
VALUES
('João Ribeiro', 'joao@example.com', '00000000000', '(11) 99999-0000', crypt('aa', gen_salt('bf')), (SELECT id FROM municipality WHERE name = 'São Paulo' AND state = 'SP'));

INSERT INTO user_model (name, email, cpf, phone, password, role, municipality_id)
VALUES
('Maria Antonieta', 'maria@example.com', '11111111111', '(11) 98888-1111', crypt('aa', gen_salt('bf')), 'moderator', (SELECT id FROM municipality WHERE name = 'Campinas' AND state = 'SP'));

INSERT INTO user_model (name, email, cpf, phone, password, role, municipality_id)
VALUES
('Carlos Costa', 'carlos@example.com', '33333333333', '(21) 97777-2222', crypt('aa', gen_salt('bf')), 'admin', (SELECT id FROM municipality WHERE name = 'Rio de Janeiro' AND state = 'RJ'));