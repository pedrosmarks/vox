INSERT INTO municipality (name, state) VALUES
                                           ('São Paulo', 'SP'),
                                           ('Campinas', 'SP'),
                                           ('Rio de Janeiro', 'RJ');

INSERT INTO user_model (name, email, cpf, phone, password, role, birth_date, municipality_id)
VALUES
    ('João Ribeiro', 'joao@example.com', '00000000000', '(11) 99999-0000', crypt('aa', gen_salt('bf')), 'CITIZEN', '1990-05-15', 1);

INSERT INTO user_model (name, email, cpf, phone, password, role, birth_date, municipality_id)
VALUES
    ('Maria Antonieta', 'maria@example.com', '11111111111', '(11) 98888-1111', crypt('aa', gen_salt('bf')), 'MODERATOR', '1985-10-22', 2);

INSERT INTO user_model (name, email, cpf, phone, password, role, birth_date, municipality_id)
VALUES
    ('Carlos Costa', 'carlos@example.com', '33333333333', '(21) 97777-2222', crypt('aa', gen_salt('bf')), 'ADMINISTRATOR', '1978-03-08', 3);

INSERT INTO category (name, description)
VALUES
(
    'Infraestrutura Urbana',
    'Projetos voltados para melhorias de ruas, praças, iluminação pública e mobilidade urbana.'
),
(
    'Saúde',
    'Projetos relacionados à melhoria dos serviços de saúde, hospitais, postos e campanhas.'
),
(
    'Educação',
    'Projetos destinados ao desenvolvimento da educação, escolas, bibliotecas e capacitação.'
);
