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
    ('João Ribeiro', 'cidadao1@example.com', '00000000000', '(11) 99999-0000', crypt('aa', gen_salt('bf')), 'CITIZEN', '1990-05-15', 1);

INSERT INTO user_model (name, email, cpf, phone, password, role, birth_date, municipality_id)
VALUES
    ('Maria Antonieta', 'moderador1@example.com', '11111111111', '(11) 98888-1111', crypt('aa', gen_salt('bf')), 'MODERATOR', '1985-10-22', 1);

INSERT INTO user_model (name, email, cpf, phone, password, role, birth_date, municipality_id)
VALUES
    ('Carlos Costa', 'administrador1@example.com', '33333333333', '(21) 97777-2222', crypt('aa', gen_salt('bf')), 'ADMINISTRATOR', '1978-03-08', 1);

INSERT INTO user_model (name, email, cpf, phone, password, role, birth_date, municipality_id)
VALUES
    ('Vereador Gabriel', 'vereador1@example.com', '22222222222', '(21) 97777-3333', crypt('aa', gen_salt('bf')), 'COUNCILOR', '1978-03-08', 1);

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

-- =====================================================================
-- DADOS MOCKADOS PARA APRESENTAÇÃO / TESTES
-- =====================================================================
-- Referências de IDs já criados acima:
--   Municípios:  1 = Santa Rita do Sapucaí (MG), 2 = Pouso Alegre (MG), 9 = São Paulo (SP)
--   Usuários:    1 = João (CITIZEN), 2 = Maria (MODERATOR), 3 = Carlos (ADMINISTRATOR), 4 = Gabriel (COUNCILOR)
--   Categorias:  1 = Infraestrutura Urbana, 2 = Saúde, 3 = Educação
-- Senha de todos os usuários: 'aa'
-- =====================================================================

-- ---------------------------------------------------------------------
-- Usuários adicionais (mais cidadãos e vereadores para enriquecer a demo)
-- ---------------------------------------------------------------------
INSERT INTO user_model (name, email, cpf, phone, password, role, birth_date, accepted_terms, accepted_privacy_policy, terms_accepted_at, municipality_id)
VALUES
    ('Ana Beatriz Souza',   'cidadao2@example.com',  '44444444444', '(35) 99123-4567', crypt('aa', gen_salt('bf')), 'CITIZEN',   '1995-07-12', TRUE, TRUE, CURRENT_TIMESTAMP, 1),
    ('Pedro Henrique Lima', 'cidadao3@example.com',  '55555555555', '(35) 99234-5678', crypt('aa', gen_salt('bf')), 'CITIZEN',   '1988-11-30', TRUE, TRUE, CURRENT_TIMESTAMP, 1),
    ('Juliana Martins',     'cidadao4@example.com',  '66666666666', '(35) 99345-6789', crypt('aa', gen_salt('bf')), 'CITIZEN',   '2000-02-18', TRUE, TRUE, CURRENT_TIMESTAMP, 2),
    ('Roberto Alves',       'cidadao5@example.com',  '77777777777', '(35) 99456-7890', crypt('aa', gen_salt('bf')), 'CITIZEN',   '1975-09-05', TRUE, TRUE, CURRENT_TIMESTAMP, 1),
    ('Vereadora Fernanda',  'vereador2@example.com', '88888888888', '(35) 99567-8901', crypt('aa', gen_salt('bf')), 'COUNCILOR', '1970-04-25', TRUE, TRUE, CURRENT_TIMESTAMP, 1),
    ('Vereador Marcelo',    'vereador3@example.com', '99999999999', '(35) 99678-9012', crypt('aa', gen_salt('bf')), 'COUNCILOR', '1968-12-10', TRUE, TRUE, CURRENT_TIMESTAMP, 2);
-- IDs resultantes: 5 = Ana, 6 = Pedro, 7 = Juliana, 8 = Roberto, 9 = Fernanda (COUNCILOR), 10 = Marcelo (COUNCILOR)

-- ---------------------------------------------------------------------
-- Categorias adicionais
-- ---------------------------------------------------------------------
INSERT INTO category (name, description)
VALUES
    ('Meio Ambiente',   'Projetos de preservação ambiental, arborização, coleta seletiva e sustentabilidade.'),
    ('Segurança',       'Projetos voltados para segurança pública, iluminação e videomonitoramento.'),
    ('Cultura e Lazer', 'Projetos de eventos culturais, esporte, praças e espaços de convivência.'),
    ('Turismo',         'Projetos de fomento ao turismo, pontos turísticos, sinalização e divulgação da cidade.');
-- IDs resultantes: 4 = Meio Ambiente, 5 = Segurança, 6 = Cultura e Lazer, 7 = Turismo

-- ---------------------------------------------------------------------
-- Configurações de usuário (acessibilidade)
-- ---------------------------------------------------------------------
INSERT INTO user_settings (user_id, font_size, accessibility_mode)
VALUES
    (1, 16, 'NONE'),
    (2, 18, 'DARK'),
    (3, 20, 'HIGH_CONTRAST'),
    (5, 22, 'PROTANOPIA'),
    (6, 16, 'NONE');

-- ---------------------------------------------------------------------
-- Projetos (mistura de CITIZEN e CHAMBER, vários status)
-- ---------------------------------------------------------------------
INSERT INTO project (municipality_id, category_id, type, title, description, status, author_id, is_official, highlighted,
                     neighborhood, street, number, latitude, longitude, start_date, expected_end_date, end_date,
                     financial_analysis, estimated_cost, approved_budget, moderation_status)
VALUES
    (1, 1, 'CITIZEN', 'Revitalização da Praça Central',
     'Proposta de revitalização da praça central com novos bancos, iluminação LED e paisagismo.',
     'PUBLISHED', 1, FALSE, TRUE,
     'Centro', 'Rua Barão de Alfenas', '100', -22.25230000, -45.70280000,
     NULL, NULL, NULL, NULL, 150000.00, NULL, 'APPROVED'),

    (1, 1, 'CITIZEN', 'Recapeamento da Avenida Cel. Joaquim',
     'Recapeamento asfáltico da avenida principal do bairro, que apresenta muitos buracos.',
     'IN_VOTING', 5, FALSE, FALSE,
     'Vila Rica', 'Avenida Cel. Joaquim', '450', -22.25510000, -45.70910000,
     NULL, NULL, NULL, NULL, 320000.00, NULL, 'APPROVED'),

    (1, 2, 'CHAMBER', 'Ampliação da UBS do Bairro Santana',
     'Projeto oficial da câmara para ampliação da Unidade Básica de Saúde do bairro Santana.',
     'IN_EXECUTION', 4, TRUE, TRUE,
     'Santana', 'Rua das Flores', '77', -22.24980000, -45.69950000,
     '2026-01-15', '2026-08-30', NULL, 'Análise financeira aprovada pela comissão de orçamento.', 780000.00, 750000.00, 'APPROVED'),

    (2, 3, 'CITIZEN', 'Nova Biblioteca Comunitária',
     'Construção de uma biblioteca comunitária com espaço de estudos e acesso à internet.',
     'PENDING_APPROVAL', 7, FALSE, FALSE,
     'Jardim Europa', 'Rua Sete de Setembro', '210', -22.23000000, -45.93000000,
     NULL, NULL, NULL, NULL, 240000.00, NULL, 'PENDING'),

    (1, 4, 'CITIZEN', 'Programa de Arborização Urbana',
     'Plantio de 500 novas árvores nativas em vias e praças da cidade.',
     'SELECTED_BY_COUNCIL', 6, FALSE, TRUE,
     'Centro', 'Rua Coronel Joaquim Inácio', '150', -22.25200000, -45.70300000,
     NULL, NULL, NULL, NULL, 95000.00, NULL, 'APPROVED'),

    (1, 5, 'CHAMBER', 'Instalação de Câmeras de Videomonitoramento',
     'Projeto oficial para instalação de câmeras de segurança em pontos estratégicos da cidade.',
     'APPROVED_BY_COUNCIL', 9, TRUE, FALSE,
     'Centro', 'Praça Central', '1', -22.25200000, -45.70300000,
     '2026-03-01', '2026-12-15', NULL, 'Orçamento aprovado com contrapartida estadual.', 520000.00, 500000.00, 'APPROVED'),

    (2, 6, 'CITIZEN', 'Reforma da Quadra Poliesportiva',
     'Reforma completa da quadra poliesportiva do bairro, incluindo cobertura e vestiários.',
     'COMPLETED', 8, FALSE, FALSE,
     'Vila Nova', 'Rua do Esporte', '30', -22.22800000, -45.92500000,
     '2025-06-01', '2025-11-30', '2025-11-20', 'Projeto concluído dentro do orçamento previsto.', 180000.00, 175000.00, 'APPROVED'),

    (1, 1, 'CITIZEN', 'Ciclovia na Avenida das Palmeiras',
     'Criação de ciclovia de 3km ligando o centro à zona escolar.',
     'REJECTED', 1, FALSE, FALSE,
     'Zona Escolar', 'Avenida das Palmeiras', '900', -22.25800000, -45.71200000,
     NULL, NULL, NULL, NULL, 410000.00, NULL, 'REJECTED');
-- IDs resultantes: 1..8

-- Projetos de Turismo (categoria 7 = Turismo)
INSERT INTO project (municipality_id, category_id, type, title, description, status, author_id, is_official, highlighted,
                     neighborhood, street, number, latitude, longitude, start_date, expected_end_date, end_date,
                     financial_analysis, estimated_cost, approved_budget, moderation_status)
VALUES
    (1, 7, 'CITIZEN', 'Sinalização Turística do Centro Histórico',
     'Instalação de placas de sinalização turística indicando pontos históricos e culturais do centro.',
     'PUBLISHED', 1, FALSE, TRUE,
     'Centro', 'Rua Barão de Alfenas', '200', -22.25260000, -45.70310000,
     NULL, NULL, NULL, NULL, 85000.00, NULL, 'APPROVED'),

    (1, 7, 'CHAMBER', 'Mirante Turístico da Serra',
     'Projeto oficial da câmara para construção de um mirante com deque panorâmico e estacionamento.',
     'IN_EXECUTION', 4, TRUE, TRUE,
     'Alto da Serra', 'Estrada do Mirante', 's/n', -22.24500000, -45.69500000,
     '2026-02-01', '2026-10-30', NULL, 'Orçamento aprovado com apoio da secretaria de turismo.', 640000.00, 620000.00, 'APPROVED');
-- IDs resultantes: 9 = Sinalização Turística, 10 = Mirante Turístico

-- ---------------------------------------------------------------------
-- Imagens dos projetos
-- ---------------------------------------------------------------------
INSERT INTO project_image (project_id, url)
VALUES
    (1, 'https://picsum.photos/seed/praca1/800/600'),
    (1, 'https://picsum.photos/seed/praca2/800/600'),
    (2, 'https://picsum.photos/seed/avenida/800/600'),
    (3, 'https://picsum.photos/seed/ubs/800/600'),
    (5, 'https://picsum.photos/seed/arvores/800/600'),
    (6, 'https://picsum.photos/seed/cameras/800/600'),
    (7, 'https://picsum.photos/seed/quadra/800/600'),
    (9, 'https://picsum.photos/seed/sinalizacao-turistica/800/600'),
    (10, 'https://picsum.photos/seed/mirante/800/600'),
    (10, 'https://picsum.photos/seed/mirante-serra/800/600');

-- ---------------------------------------------------------------------
-- Vereadores associados a projetos (project_councilor)
-- ---------------------------------------------------------------------
INSERT INTO project_councilor (project_id, councilor_id)
VALUES
    (1, 4),
    (1, 9),
    (3, 4),
    (5, 9),
    (6, 9),
    (6, 10);

-- ---------------------------------------------------------------------
-- Opiniões / votos em projetos (respeitando UNIQUE(project_id, user_id))
-- ---------------------------------------------------------------------
INSERT INTO project_opinion (project_id, user_id, opinion)
VALUES
    (1, 1, 'APPROVE'),
    (1, 5, 'APPROVE'),
    (1, 6, 'NEUTRAL'),
    (1, 8, 'APPROVE'),
    (2, 1, 'APPROVE'),
    (2, 5, 'NEUTRAL'),
    (2, 7, 'APPROVE'),
    (5, 6, 'APPROVE'),
    (5, 8, 'APPROVE'),
    (5, 1, 'NEUTRAL'),
    (8, 5, 'NEUTRAL'),
    (8, 6, 'NEUTRAL');

-- ---------------------------------------------------------------------
-- Assinaturas de apoio a projetos (project_signature)
-- ---------------------------------------------------------------------
INSERT INTO project_signature (project_id, user_id)
VALUES
    (1, 1), (1, 5), (1, 6), (1, 8),
    (2, 5), (2, 7),
    (5, 1), (5, 6), (5, 8);

-- ---------------------------------------------------------------------
-- Moderação de projetos
-- ---------------------------------------------------------------------
INSERT INTO project_moderation (project_id, moderator_id, action, feedback)
VALUES
    (1, 2, 'APPROVED', 'Projeto claro e bem fundamentado. Aprovado para publicação.'),
    (2, 2, 'APPROVED', 'Aprovado. Recomenda-se anexar orçamento detalhado.'),
    (5, 2, 'APPROVED', 'Excelente iniciativa ambiental.'),
    (8, 2, 'REJECTED', 'Custo acima do previsto para o orçamento atual. Reavaliar traçado.');

-- ---------------------------------------------------------------------
-- Histórico de status de projetos
-- ---------------------------------------------------------------------
INSERT INTO project_status_history (project_id, previous_status, new_status, changed_by, note)
VALUES
    (1, 'PENDING_APPROVAL', 'PUBLISHED', 2, 'Aprovado pela moderação e publicado.'),
    (2, 'PENDING_APPROVAL', 'PUBLISHED', 2, 'Publicado após aprovação.'),
    (2, 'PUBLISHED', 'IN_VOTING', 3, 'Aberta a fase de votação popular.'),
    (3, 'APPROVED_BY_COUNCIL', 'IN_EXECUTION', 3, 'Obra iniciada.'),
    (6, 'SELECTED_BY_COUNCIL', 'APPROVED_BY_COUNCIL', 3, 'Aprovado pela câmara com orçamento definido.'),
    (7, 'IN_EXECUTION', 'COMPLETED', 3, 'Obra concluída e entregue à comunidade.'),
    (8, 'PENDING_APPROVAL', 'REJECTED', 2, 'Rejeitado na moderação.');

-- ---------------------------------------------------------------------
-- Denúncias / reportes de problemas (issue_report)
-- councilor_id é NOT NULL -> usar um vereador válido
-- ---------------------------------------------------------------------
INSERT INTO issue_report (municipality_id, category_id, author_id, councilor_id, title, description, neighborhood, street, number,
                          latitude, longitude, status, moderation_status)
VALUES
    (1, 1, 1, null, 'Buraco na via', 'Buraco grande na Rua Barão de Alfenas causando risco a motociclistas.',
     'Centro', 'Rua Barão de Alfenas', '150', -22.25240000, -45.70290000, 'OPEN', 'PENDING'),
    (1, 1, 5, 4, 'Poste de luz queimado', 'Iluminação pública apagada há mais de duas semanas na Vila Rica.',
     'Vila Rica', 'Avenida Cel. Joaquim', '460', -22.25520000, -45.70920000, 'UNDER_REVIEW', 'APPROVED'),
    (1, 4, 6, 9, 'Lixo acumulado', 'Acúmulo de lixo em terreno baldio atraindo insetos.',
     'Santana', 'Rua das Flores', '80', -22.24990000, -45.69960000, 'IN_PROGRESS', 'APPROVED'),
    (2, 1, 7, 10, 'Vazamento de água', 'Vazamento de água tratada na calçada há vários dias.',
     'Jardim Europa', 'Rua Sete de Setembro', '215', -22.23010000, -45.93010000, 'RESOLVED', 'APPROVED'),
    (1, 1, 8, 4, 'Sinalização apagada', 'Faixa de pedestres e placas de trânsito apagadas próximo à escola.',
     'Zona Escolar', 'Avenida das Palmeiras', '905', -22.25810000, -45.71210000, 'OPEN', 'PENDING');
-- IDs resultantes: 1..5

-- Denúncias de Turismo (categoria 7 = Turismo)
INSERT INTO issue_report (municipality_id, category_id, author_id, councilor_id, title, description, neighborhood, street, number,
                          latitude, longitude, status, moderation_status)
VALUES
    (1, 7, 6, null, 'Placa turística danificada', 'Placa indicativa do centro histórico está quebrada e ilegível para os visitantes.',
     'Centro', 'Praça da Matriz', '10', -22.25270000, -45.70320000, 'OPEN', 'APPROVED'),
    (1, 7, 8, 4, 'Mirante sem manutenção', 'Guarda-corpo do mirante turístico está enferrujado, oferecendo risco aos turistas.',
     'Alto da Serra', 'Estrada do Mirante', 's/n', -22.24510000, -45.69510000, 'UNDER_REVIEW', 'APPROVED');
-- IDs resultantes: 6 = Placa turística, 7 = Mirante sem manutenção

-- ---------------------------------------------------------------------
-- Imagens das denúncias
-- ---------------------------------------------------------------------
INSERT INTO issue_image (issue_id, url)
VALUES
    (1, 'https://picsum.photos/seed/buraco/800/600'),
    (2, 'https://picsum.photos/seed/poste/800/600'),
    (3, 'https://picsum.photos/seed/lixo/800/600'),
    (4, 'https://picsum.photos/seed/vazamento/800/600'),
    (6, 'https://picsum.photos/seed/placa-turistica/800/600'),
    (7, 'https://picsum.photos/seed/mirante-manutencao/800/600');

-- ---------------------------------------------------------------------
-- Moderação de denúncias
-- ---------------------------------------------------------------------
INSERT INTO issue_moderation (issue_id, moderator_id, action, feedback)
VALUES
    (2, 2, 'APPROVED', 'Denúncia pertinente, encaminhada ao setor responsável.'),
    (3, 2, 'APPROVED', 'Confirmado o acúmulo de lixo, em andamento.'),
    (4, 2, 'APPROVED', 'Problema resolvido pela concessionária.');

-- ---------------------------------------------------------------------
-- Histórico de status das denúncias
-- ---------------------------------------------------------------------
INSERT INTO issue_status_history (issue_id, previous_status, new_status, changed_by, note)
VALUES
    (2, 'OPEN', 'UNDER_REVIEW', 2, 'Em análise pela moderação.'),
    (3, 'OPEN', 'UNDER_REVIEW', 2, 'Em análise.'),
    (3, 'UNDER_REVIEW', 'IN_PROGRESS', 4, 'Equipe de limpeza acionada.'),
    (4, 'OPEN', 'IN_PROGRESS', 10, 'Concessionária notificada.'),
    (4, 'IN_PROGRESS', 'RESOLVED', 10, 'Vazamento reparado.');

-- ---------------------------------------------------------------------
-- Salas de conferência / audiências públicas
-- ---------------------------------------------------------------------
INSERT INTO conference_room (name, description, moderator_id, municipality_id, status)
VALUES
    ('Audiência Pública - Orçamento 2026', 'Discussão do orçamento participativo para o ano de 2026.', 2, 1, 'OPEN'),
    ('Sessão sobre Mobilidade Urbana', 'Debate sobre ciclovias e transporte público.', 2, 1, 'OPEN'),
    ('Reunião de Prestação de Contas', 'Prestação de contas do primeiro semestre.', 3, 2, 'CLOSED');
-- IDs resultantes: 1..3

-- ---------------------------------------------------------------------
-- Participantes das salas
-- ---------------------------------------------------------------------
INSERT INTO room_participant (room_id, user_id, status, can_publish_audio, can_publish_video, decided_at)
VALUES
    (1, 1, 'APPROVED', TRUE,  FALSE, CURRENT_TIMESTAMP),
    (1, 5, 'APPROVED', FALSE, FALSE, CURRENT_TIMESTAMP),
    (1, 6, 'PENDING',  FALSE, FALSE, NULL),
    (2, 1, 'APPROVED', TRUE,  TRUE,  CURRENT_TIMESTAMP),
    (2, 8, 'REJECTED', FALSE, FALSE, CURRENT_TIMESTAMP),
    (3, 7, 'APPROVED', TRUE,  FALSE, CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------------
-- Assinaturas de acompanhamento (subscription)
-- ---------------------------------------------------------------------
INSERT INTO subscription (user_id, type, project_id, issue_id, category_id, councilor_id)
VALUES
    (1, 'ALL_PROJECTS', NULL, NULL, NULL, NULL),
    (1, 'PROJECT',      1,    NULL, NULL, NULL),
    (5, 'CATEGORY',     NULL, NULL, 1,    NULL),
    (6, 'COUNCILOR',    NULL, NULL, NULL, 4),
    (7, 'ISSUE',        NULL, 4,    NULL, NULL),
    (8, 'ALL_ISSUES',   NULL, NULL, NULL, NULL);

-- ---------------------------------------------------------------------
-- Notificações
-- ---------------------------------------------------------------------
INSERT INTO notification (user_id, title, message, type, read, read_at)
VALUES
    (1, 'Seu projeto foi publicado', 'O projeto "Revitalização da Praça Central" foi aprovado e publicado.', 'PROJECT_STATUS_CHANGED', TRUE, CURRENT_TIMESTAMP),
    (5, 'Novo projeto na sua categoria', 'Um novo projeto de Infraestrutura Urbana foi criado.', 'PROJECT_CREATED', FALSE, NULL),
    (6, 'Você foi marcado em um projeto', 'Você foi mencionado no projeto "Programa de Arborização Urbana".', 'PROJECT_TAGGED', FALSE, NULL),
    (7, 'Sua denúncia foi resolvida', 'A denúncia "Vazamento de água" foi marcada como resolvida.', 'ISSUE_STATUS_CHANGED', TRUE, CURRENT_TIMESTAMP),
    (4, 'Nova denúncia atribuída', 'Uma nova denúncia foi atribuída a você.', 'ISSUE_CREATED', FALSE, NULL),
    (1, 'Atualização de projeto', 'O projeto "Recapeamento da Avenida Cel. Joaquim" entrou em votação.', 'PROJECT_UPDATED', FALSE, NULL);

-- ---------------------------------------------------------------------
-- Categorias de evento (tabela dedicada, separada de category)
-- ---------------------------------------------------------------------
INSERT INTO event_category (name, description)
VALUES
    ('Cultura',      'Shows, exposições, teatro e eventos culturais.'),
    ('Esporte',      'Competições, corridas e atividades esportivas.'),
    ('Educação',     'Palestras, workshops, cursos e feiras educativas.'),
    ('Comunitário',  'Encontros comunitários, mutirões e ações sociais.'),
    ('Tecnologia',   'Hackathons, meetups e eventos de inovação.'),
    ('Turismo',      'Passeios, festivais turísticos e eventos de valorização da cidade.');

-- ---------------------------------------------------------------------
-- Eventos (sem limitação de município: municípios variados de propósito)
-- Autores: 2 = MODERATOR, 3 = ADMINISTRATOR
-- Datas relativas a CURRENT_DATE para manter a demo sempre com eventos futuros
-- ---------------------------------------------------------------------
INSERT INTO event (title, description, category_id, price, start_date, end_date, neighborhood, street, number, latitude, longitude, municipality_id, author_id)
VALUES
    ('Festival de Inverno',
     'Três dias de shows, feira gastronômica e apresentações culturais na praça central.',
     1, 30.00,
     CURRENT_DATE + INTERVAL '10 days' + TIME '18:00',
     CURRENT_DATE + INTERVAL '12 days' + TIME '23:00',
     'Centro', 'Praça João Pessoa', 'S/N', -22.23015000, -45.93610000, 1, 2),

    ('Corrida da Cidade 10K',
     'Corrida de rua com percursos de 5K e 10K, aberta a todas as idades.',
     2, 0.00,
     CURRENT_DATE + INTERVAL '20 days' + TIME '07:00',
     CURRENT_DATE + INTERVAL '20 days' + TIME '11:00',
     'Centro', 'Avenida Doutor Lisboa', '1000', -22.22780000, -45.93420000, 1, 2),

    ('Feira de Ciências e Tecnologia',
     'Exposição de projetos de escolas e startups locais, com palestras e oficinas.',
     3, NULL,
     CURRENT_DATE + INTERVAL '5 days' + TIME '09:00',
     CURRENT_DATE + INTERVAL '5 days' + TIME '17:00',
     'Fátima', 'Rua das Palmeiras', '250', -22.24120000, -45.92510000, 9, 3),

    ('Mutirão de Limpeza do Rio',
     'Ação comunitária de limpeza das margens do rio, com café da manhã para voluntários.',
     4, 0.00,
     CURRENT_DATE + INTERVAL '3 days' + TIME '08:00',
     CURRENT_DATE + INTERVAL '3 days' + TIME '12:00',
     'São Geraldo', 'Rua Beira Rio', 'S/N', -22.23540000, -45.94080000, 1, 2),

    ('Hackathon Cidades Inteligentes',
     'Maratona de programação de 48h focada em soluções para gestão pública municipal.',
     5, 50.00,
     CURRENT_DATE + INTERVAL '30 days' + TIME '19:00',
     CURRENT_DATE + INTERVAL '32 days' + TIME '19:00',
     'Cidade Jardim', 'Rua da Inovação', '45', -22.22190000, -45.92870000, 17, 3),

    ('Sarau Cultural de Primavera',
     'Noite de poesia, música acústica e feira de artesanato local.',
     1, 15.00,
     CURRENT_DATE + INTERVAL '45 days' + TIME '20:00',
     CURRENT_DATE + INTERVAL '45 days' + TIME '23:30',
     'Centro', 'Rua Comendador José Garcia', '320', -22.22960000, -45.93770000, 1, 2),

    -- Eventos de Turismo (categoria 6 = Turismo em event_category)
    ('City Tour Histórico',
     'Passeio guiado gratuito pelos principais pontos históricos e culturais da cidade.',
     6, 0.00,
     CURRENT_DATE + INTERVAL '7 days' + TIME '09:00',
     CURRENT_DATE + INTERVAL '7 days' + TIME '12:00',
     'Centro', 'Praça da Matriz', 'S/N', -22.22870000, -45.93650000, 1, 2),

    ('Festival de Turismo Rural',
     'Feira com produtores locais, gastronomia caipira e passeios pelas fazendas históricas da região.',
     6, 25.00,
     CURRENT_DATE + INTERVAL '25 days' + TIME '10:00',
     CURRENT_DATE + INTERVAL '27 days' + TIME '18:00',
     'Zona Rural', 'Estrada do Parque de Exposições', 'km 3', -22.20450000, -45.95120000, 1, 3),

    -- Eventos reais de Santa Rita do Sapucaí (município 1)
    ('Carnaval Oficial da Cidade',
     'A maior festa carnavalesca do Sul de Minas Gerais. Reúne programação municipal com blocos de rua no Centro de Eventos.',
     1, 0.00,
     TIMESTAMP '2026-02-13 00:00', TIMESTAMP '2026-02-17 23:59',
     'Santa Rita do Sapucaí', 'R. Sete', '245', -22.25200000, -45.70300000, 1, 3),

    ('Bloco do Urso',
     'O grandioso evento privado de escala nacional Bloco do Urso, que conta com atrações de peso como Alok, Ivete Sangalo e Léo Santana em uma arena open bar.',
     1, 500.00,
     TIMESTAMP '2026-02-13 00:00', TIMESTAMP '2026-02-17 23:59',
     'Cidade do Urso', 'Cidade do Urso', 'S/N', -22.25600000, -45.71000000, 1, 3),

    ('Fest Rock Sul de Minas (14ª Edição)',
     'Tradicional encontro de rock da cidade com apresentações de bandas regionais e tributos oficiais, contando com praça de alimentação e área de convivência.',
     1, 0.00,
     TIMESTAMP '2026-05-16 16:00', TIMESTAMP '2026-05-17 00:00',
     'Santa Rita do Sapucaí', 'R. Sete', '245', -22.25200000, -45.70300000, 1, 3),

    ('Festa da Padroeira Santa Rita de Cássia',
     'Uma das celebrações religiosas e culturais mais tradicionais da região. Além do tríduo e procissões, conta com grandiosa praça de alimentação e grandes shows nacionais (como Paralamas do Sucesso, Edson & Hudson, Katinguelê) organizados pela Prefeitura.',
     1, 0.00,
     TIMESTAMP '2026-05-13 00:00', TIMESTAMP '2026-05-24 23:59',
     'Centro', 'Praça Santa Rita', '113', -22.25230000, -45.70260000, 1, 3),

    ('Feirão Folclórico 2026',
     'Evento multicultural focado nas tradições locais, apresentações sertanejas (Bruna Viola, Felipe & Falcão), Encontro de Carros Antigos e Show de Calouros.',
     1, 0.00,
     TIMESTAMP '2026-08-14 00:00', TIMESTAMP '2026-08-17 23:59',
     'Santa Rita do Sapucaí', 'R. Sete', '245', -22.25200000, -45.70300000, 1, 3),

    ('Eros Prado - Stand-up "A Quinta Série Venceu"',
     'Apresentação de comédia solo com o humorista Eros Prado, reunindo piadas, interações com a plateia e personagens cômicos.',
     1, 50.00,
     TIMESTAMP '2026-09-26 20:00', TIMESTAMP '2026-09-26 21:30',
     'Centro', 'Praça Santa Rita', 'S/N', -22.25230000, -45.70260000, 1, 3),

    ('HackTown 2026',
     'Considerado o maior festival de inovação, tecnologia, criatividade e música do Brasil. Reúne centenas de palestras simultâneas, workshops, shows e atrações distribuídas por toda a cidade em um formato similar ao SXSW americano.',
     5, 500.00,
     TIMESTAMP '2026-10-01 00:00', TIMESTAMP '2026-10-04 23:59',
     'Centro', 'Inatel, FAI, ETE e múltiplos pontos urbanos', 'S/N', -22.25730000, -45.70590000, 1, 3),

    ('FAITEC 2026 (37ª Feira de Tecnologia e Empreendedorismo da FAI)',
     'Exposição anual de projetos tecnológicos, soluções digitais, protótipos de software e planos de negócios inovadores da FAI. O evento atrai investidores, empresas parceiras do ecossistema do Vale da Eletrônica e visitantes de toda a região.',
     5, 0.00,
     TIMESTAMP '2026-09-30 00:00', TIMESTAMP '2026-10-02 23:59',
     'Centro', 'Av. Sinhá Moreira', '161-221', -22.25400000, -45.70450000, 1, 3);

-- ---------------------------------------------------------------------
-- Imagens de eventos (alguns eventos sem imagem para exercitar o filtro hasImage)
-- ---------------------------------------------------------------------
INSERT INTO event_image (event_id, url)
VALUES
    (1, 'https://picsum.photos/seed/festival-inverno/800/600'),
    (1, 'https://picsum.photos/seed/festival-inverno-2/800/600'),
    (2, 'https://picsum.photos/seed/corrida-10k/800/600'),
    (3, 'https://picsum.photos/seed/feira-ciencias/800/600'),
    (5, 'https://picsum.photos/seed/hackathon/800/600'),
    (7, 'https://picsum.photos/seed/city-tour/800/600'),
    (8, 'https://picsum.photos/seed/turismo-rural/800/600'),
    (9,  'https://res.cloudinary.com/dohegns1y/image/upload/v1790347338/carnaval_ejyg9a.jpg'),
    (10, 'https://res.cloudinary.com/dohegns1y/image/upload/v1790347330/bloco_do_urso_q9zvmv.avif'),
    (11, 'https://res.cloudinary.com/dohegns1y/image/upload/v1790347515/festa_rock_rxppbv.png'),
    (12, 'https://res.cloudinary.com/dohegns1y/image/upload/v1790347800/IMG_20260925_114907.jpg_yjzvmh.jpg'),
    (13, 'https://res.cloudinary.com/dohegns1y/image/upload/v1790347935/feirao_vrpec6.jpg'),
    (14, 'https://res.cloudinary.com/dohegns1y/image/upload/v1790347980/eros_gvcc17.jpg'),
    (15, 'https://res.cloudinary.com/dohegns1y/image/upload/v1790348139/hacktown_jvt9do.jpg'),
    (16, 'https://res.cloudinary.com/dohegns1y/image/upload/v1790348207/faitec_vla23f.jpg');
