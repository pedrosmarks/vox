INSERT INTO municipality (name, state) VALUES
    -- Minas Gerais
    ('Santa Rita do Sapucaí', 'MG'),
    ('Pouso Alegre', 'MG'),
    ('São Sebastião da Bela Vista', 'MG'),
    ('Cachoeira de Minas', 'MG'),
    ('Careaçu', 'MG'),
    ('São Gonçalo do Sapucaí', 'MG'),
    ('Itajubá', 'MG'),
    ('Três Corações', 'MG'),

    -- São Paulo
    ('São Paulo', 'SP'),
    ('Campinas', 'SP'),
    ('Bragança Paulista', 'SP'),
    ('Atibaia', 'SP'),
    ('Mogi das Cruzes', 'SP'),
    ('São José dos Campos', 'SP'),
    ('Guarulhos', 'SP'),
    ('Taubaté', 'SP'),

    -- Rio de Janeiro
    ('Rio de Janeiro', 'RJ'),
    ('Volta Redonda', 'RJ'),
    ('Resende', 'RJ'),
    ('Barra Mansa', 'RJ'),
    ('Petrópolis', 'RJ'),
    ('Nova Friburgo', 'RJ'),
    ('Angra dos Reis', 'RJ'),

    -- Espírito Santo
    ('Vitória', 'ES'),
    ('Vila Velha', 'ES'),
    ('Serra', 'ES'),
    ('Cariacica', 'ES'),
    ('Linhares', 'ES'),
    ('Cachoeiro de Itapemirim', 'ES'),

    -- Paraná
    ('Curitiba', 'PR'),
    ('Londrina', 'PR'),
    ('Maringá', 'PR'),
    ('Ponta Grossa', 'PR'),
    ('Cascavel', 'PR'),
    ('São José dos Pinhais', 'PR'),

    -- Bahia
    ('Salvador', 'BA'),
    ('Feira de Santana', 'BA'),
    ('Vitória da Conquista', 'BA'),
    ('Juazeiro', 'BA'),
    ('Ilhéus', 'BA'),
    ('Itabuna', 'BA'),

    -- Goiás
    ('Goiânia', 'GO'),
    ('Aparecida de Goiânia', 'GO'),
    ('Anápolis', 'GO'),
    ('Rio Verde', 'GO'),
    ('Luziânia', 'GO'),
    ('Catalão', 'GO'),

    -- Santa Catarina
    ('Florianópolis', 'SC'),
    ('Joinville', 'SC'),
    ('Blumenau', 'SC'),
    ('Chapecó', 'SC'),
    ('Itajaí', 'SC'),
    ('São José', 'SC');


INSERT INTO user_model (name, email, cpf, phone, password, role, birth_date, municipality_id)
VALUES
    ('João Ribeiro', 'joao@example.com', '00000000000', '(11) 99999-0000', crypt('aa', gen_salt('bf')), 'CITIZEN', '1990-05-15', 1);

INSERT INTO user_model (name, email, cpf, phone, password, role, birth_date, municipality_id)
VALUES
    ('Maria Antonieta', 'maria@example.com', '11111111111', '(11) 98888-1111', crypt('aa', gen_salt('bf')), 'MODERATOR', '1985-10-22', 1);

INSERT INTO user_model (name, email, cpf, phone, password, role, birth_date, municipality_id)
VALUES
    ('Carlos Costa', 'carlos@example.com', '33333333333', '(21) 97777-2222', crypt('aa', gen_salt('bf')), 'ADMINISTRATOR', '1978-03-08', 1);

INSERT INTO user_model (name, email, cpf, phone, password, role, birth_date, municipality_id)
VALUES
    ('Dito', 'dito@example.com', '22222222222', '(21) 97777-3333', crypt('aa', gen_salt('bf')), 'COUNCILOR', '1978-03-08', 1);

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
