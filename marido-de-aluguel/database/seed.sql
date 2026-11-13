-- Marido de Aluguel
-- Dados de exemplo (Maringá-PR). Executar depois do schema.sql.
-- Senha de todos os usuários: senha123 (armazenada como hash BCrypt).
-- Coordenadas aproximadas dos bairros, apenas para demonstrar o cálculo de distância.

-- Clientes
INSERT INTO usuarios (id, nome, email, telefone, senha_hash, endereco, latitude, longitude, tipo_usuario) VALUES
(1, 'Camila Ferreira',  'camila@email.com',   '(44) 99811-2233', '$2a$10$kJSnnyLOf62D9kgO.sUune07DgA916Q8l9VENdF.FW1DBDxxDTCT2', 'Av. Cerro Azul, 1200 - Zona 07, Maringá',        -23.4105, -51.9372, 'CLIENTE'),
(2, 'Rafael Souza',     'rafael@email.com',   '(44) 99722-3344', '$2a$10$kJSnnyLOf62D9kgO.sUune07DgA916Q8l9VENdF.FW1DBDxxDTCT2', 'Rua Pioneiro José Bonifácio, 85 - Jd. Alvorada', -23.3940, -51.9530, 'CLIENTE'),
(3, 'Juliana Prado',    'juliana@email.com',  '(44) 99633-4455', '$2a$10$kJSnnyLOf62D9kgO.sUune07DgA916Q8l9VENdF.FW1DBDxxDTCT2', 'Rua Santos Dumont, 430 - Zona 02, Maringá',       -23.4300, -51.9280, 'CLIENTE');

-- Prestadores (nota_media e quantidade_avaliacoes são recalculadas no fim do script)
INSERT INTO usuarios (id, nome, email, telefone, senha_hash, endereco, latitude, longitude, tipo_usuario) VALUES
(4,  'Douglas Oliveira', 'douglas@email.com',  '(44) 99901-1122', '$2a$10$kJSnnyLOf62D9kgO.sUune07DgA916Q8l9VENdF.FW1DBDxxDTCT2', 'Rua Néo Alves Martins, 300 - Zona 05, Maringá',   -23.4196, -51.9497, 'PRESTADOR'),
(5,  'Marcos Lima',      'marcos@email.com',   '(44) 99802-2211', '$2a$10$kJSnnyLOf62D9kgO.sUune07DgA916Q8l9VENdF.FW1DBDxxDTCT2', 'Av. Mauá, 1500 - Vila Operária, Maringá',         -23.4165, -51.9235, 'PRESTADOR'),
(6,  'Sérgio Andrade',   'sergio@email.com',   '(44) 99703-3322', '$2a$10$kJSnnyLOf62D9kgO.sUune07DgA916Q8l9VENdF.FW1DBDxxDTCT2', 'Rua Pioneiro Antônio Ruiz, 77 - Jd. Alvorada',    -23.3945, -51.9545, 'PRESTADOR'),
(7,  'Paulo Mendes',     'paulo@email.com',    '(44) 99604-4433', '$2a$10$kJSnnyLOf62D9kgO.sUune07DgA916Q8l9VENdF.FW1DBDxxDTCT2', 'Av. Brasil, 2500 - Zona 01, Maringá',             -23.4253, -51.9386, 'PRESTADOR'),
(8,  'Ana Ribeiro',      'ana@email.com',      '(44) 99505-5544', '$2a$10$kJSnnyLOf62D9kgO.sUune07DgA916Q8l9VENdF.FW1DBDxxDTCT2', 'Rua Pioneira Maria Gimenes, 210 - Mandacaru',     -23.3925, -51.9290, 'PRESTADOR'),
(9,  'Roberto Teixeira', 'roberto@email.com',  '(44) 99406-6655', '$2a$10$kJSnnyLOf62D9kgO.sUune07DgA916Q8l9VENdF.FW1DBDxxDTCT2', 'Av. Londrina, 900 - Centro, Sarandi',             -23.4440, -51.8740, 'PRESTADOR'),
(10, 'Luciana Costa',    'luciana@email.com',  '(44) 99307-7766', '$2a$10$kJSnnyLOf62D9kgO.sUune07DgA916Q8l9VENdF.FW1DBDxxDTCT2', 'Rua Pioneiro Alcides Araújo, 45 - Jd. Novo Horizonte', -23.4405, -51.9440, 'PRESTADOR');

-- Serviços oferecidos (RF02: categoria, descrição, raio, modelo de cobrança e valor base)
INSERT INTO servicos (id, prestador_id, categoria, descricao, raio_atendimento_km, tipo_cobranca, valor_base) VALUES
(1,  4,  'Elétrica',           'Troca de resistência de chuveiro, com teste de aterramento',          10, 'POR_EMPREITADA', 80.00),
(2,  4,  'Elétrica',           'Instalação elétrica residencial: fiação, disjuntores e pontos',        10, 'POR_HORA',       75.00),
(3,  4,  'Hidráulica',         'Conserto de vazamentos em torneiras, registros e caixas acopladas',   10, 'POR_HORA',       70.00),
(4,  5,  'Montagem de móveis', 'Montagem de guarda-roupa de até 6 portas, com fixação na parede',     15, 'POR_EMPREITADA', 180.00),
(5,  5,  'Montagem de móveis', 'Montagem de racks, cômodas, camas e escrivaninhas',                   15, 'POR_HORA',       60.00),
(6,  6,  'Elétrica',           'Instalação de tomadas, interruptores e dimmers',                       8, 'POR_HORA',       65.00),
(7,  6,  'Pintura',            'Pintura de cômodo de até 15 m², duas demãos (tinta do cliente)',       8, 'POR_EMPREITADA', 450.00),
(8,  7,  'Hidráulica',         'Desentupimento de pias, ralos e sifões',                              12, 'POR_EMPREITADA', 120.00),
(9,  7,  'Elétrica',           'Instalação de chuveiro ou torneira elétrica',                         12, 'POR_EMPREITADA', 95.00),
(10, 8,  'Pintura',            'Pintura residencial interna e externa, com massa corrida',            10, 'POR_HORA',       55.00),
(11, 8,  'Montagem de móveis', 'Instalação de prateleiras, nichos e suportes de TV',                  10, 'POR_EMPREITADA', 90.00),
(12, 9,  'Elétrica',           'Manutenção elétrica em Sarandi e divisa com Maringá',                  4, 'POR_HORA',       60.00),
(13, 10, 'Hidráulica',         'Troca de registro de gaveta/pressão e de sifão',                      10, 'POR_EMPREITADA', 110.00),
(14, 10, 'Pequenos reparos',   'Ajuste de portas, fechaduras, varais e pequenos consertos',           10, 'POR_HORA',       50.00),
(15, 5,  'Pequenos reparos',   'Instalação de cortinas, varões, trilhos e persianas',                 15, 'POR_EMPREITADA', 70.00);

-- Ordens de serviço
INSERT INTO ordens_servico (id, cliente_id, servico_id, data_abertura, status, horas_estimadas, valor_final) VALUES
(1,  1, 1,  '2026-07-03', 'CONCLUIDA',   NULL, 80.00),
(2,  2, 2,  '2026-08-10', 'CONCLUIDA',   3.00, 225.00),
(3,  3, 3,  '2026-09-15', 'CONCLUIDA',   2.00, 140.00),
(4,  1, 8,  '2026-08-20', 'CONCLUIDA',   NULL, 120.00),
(5,  2, 9,  '2026-10-01', 'CONCLUIDA',   NULL, 95.00),
(6,  1, 4,  '2026-09-02', 'CONCLUIDA',   NULL, 180.00),
(7,  3, 7,  '2026-10-12', 'CONCLUIDA',   NULL, 450.00),
(8,  2, 10, '2026-10-20', 'CONCLUIDA',   6.00, 330.00),
(9,  1, 6,  '2026-10-28', 'CONCLUIDA',   2.00, 130.00),
(10, 1, 13, '2026-11-02', 'ACEITA',      NULL, 110.00),
(11, 3, 2,  '2026-11-03', 'ABERTA',      2.00, 150.00),
(12, 2, 5,  '2026-10-05', 'CANCELADA',   2.00, 120.00),
(13, 3, 1,  '2026-10-25', 'CONCLUIDA',   NULL, 80.00);

-- Histórico de status (RF05)
INSERT INTO ordem_status_historico (ordem_id, status, data_hora) VALUES
(1, 'ABERTA', '2026-07-03 09:12'), (1, 'ACEITA', '2026-07-03 10:05'), (1, 'EM_EXECUCAO', '2026-07-04 08:30'), (1, 'CONCLUIDA', '2026-07-04 11:00'),
(2, 'ABERTA', '2026-08-10 14:20'), (2, 'ACEITA', '2026-08-10 16:00'), (2, 'EM_EXECUCAO', '2026-08-12 08:00'), (2, 'CONCLUIDA', '2026-08-12 11:10'),
(3, 'ABERTA', '2026-09-15 08:45'), (3, 'ACEITA', '2026-09-15 09:30'), (3, 'EM_EXECUCAO', '2026-09-16 13:00'), (3, 'CONCLUIDA', '2026-09-16 15:05'),
(4, 'ABERTA', '2026-08-20 19:02'), (4, 'ACEITA', '2026-08-21 07:40'), (4, 'EM_EXECUCAO', '2026-08-21 09:00'), (4, 'CONCLUIDA', '2026-08-21 10:15'),
(5, 'ABERTA', '2026-10-01 10:00'), (5, 'ACEITA', '2026-10-01 10:30'), (5, 'EM_EXECUCAO', '2026-10-02 14:00'), (5, 'CONCLUIDA', '2026-10-02 15:00'),
(6, 'ABERTA', '2026-09-02 11:11'), (6, 'ACEITA', '2026-09-02 12:00'), (6, 'EM_EXECUCAO', '2026-09-04 08:00'), (6, 'CONCLUIDA', '2026-09-04 12:30'),
(7, 'ABERTA', '2026-10-12 08:00'), (7, 'ACEITA', '2026-10-12 09:20'), (7, 'EM_EXECUCAO', '2026-10-14 07:30'), (7, 'CONCLUIDA', '2026-10-15 17:00'),
(8, 'ABERTA', '2026-10-20 13:00'), (8, 'ACEITA', '2026-10-20 15:45'), (8, 'EM_EXECUCAO', '2026-10-22 08:00'), (8, 'CONCLUIDA', '2026-10-22 14:30'),
(9, 'ABERTA', '2026-10-28 09:00'), (9, 'ACEITA', '2026-10-28 09:40'), (9, 'EM_EXECUCAO', '2026-10-30 08:00'), (9, 'CONCLUIDA', '2026-10-30 10:20'),
(10, 'ABERTA', '2026-11-02 18:30'), (10, 'ACEITA', '2026-11-03 08:10'),
(11, 'ABERTA', '2026-11-03 20:15'),
(12, 'ABERTA', '2026-10-05 10:00'), (12, 'CANCELADA', '2026-10-05 17:20'),
(13, 'ABERTA', '2026-10-25 08:00'), (13, 'ACEITA', '2026-10-25 08:30'), (13, 'EM_EXECUCAO', '2026-10-26 09:00'), (13, 'CONCLUIDA', '2026-10-26 10:00');

-- Avaliações: só de ordens concluídas, uma por ordem (a ordem 9 fica sem avaliação para a demonstração)
INSERT INTO avaliacoes (ordem_id, nota, comentario, data_avaliacao) VALUES
(1,  5, 'Chegou no horário e resolveu rápido. Recomendo.',             '2026-07-04'),
(2,  4, 'Serviço bem feito, só atrasou um pouco para começar.',        '2026-08-12'),
(3,  5, 'Encontrou o vazamento na hora. Muito atencioso.',             '2026-09-16'),
(4,  3, 'Resolveu, mas deixou um pouco de sujeira.',                   '2026-08-21'),
(5,  4, 'Instalação correta, preço justo.',                            '2026-10-02'),
(6,  5, 'Guarda-roupa montado com capricho e bem nivelado.',           '2026-09-04'),
(7,  4, 'Pintura uniforme, cumpriu o prazo.',                          '2026-10-15'),
(8,  5, 'Excelente acabamento, deixou tudo limpo.',                    '2026-10-22'),
(13, 4, 'Bom atendimento, voltaria a contratar.',                      '2026-10-26');

-- Média e quantidade de avaliações coerentes com a tabela avaliacoes
UPDATE usuarios u SET
    nota_media = sub.media,
    quantidade_avaliacoes = sub.qtd
FROM (
    SELECT s.prestador_id, ROUND(AVG(a.nota)::numeric, 2) AS media, COUNT(*) AS qtd
    FROM avaliacoes a
    JOIN ordens_servico o ON o.id = a.ordem_id
    JOIN servicos s ON s.id = o.servico_id
    GROUP BY s.prestador_id
) sub
WHERE u.id = sub.prestador_id;

-- Ajusta as sequências após inserts com id explícito
SELECT setval(pg_get_serial_sequence('usuarios', 'id'),               (SELECT MAX(id) FROM usuarios));
SELECT setval(pg_get_serial_sequence('servicos', 'id'),               (SELECT MAX(id) FROM servicos));
SELECT setval(pg_get_serial_sequence('ordens_servico', 'id'),         (SELECT MAX(id) FROM ordens_servico));
SELECT setval(pg_get_serial_sequence('ordem_status_historico', 'id'), (SELECT MAX(id) FROM ordem_status_historico));
SELECT setval(pg_get_serial_sequence('avaliacoes', 'id'),             (SELECT MAX(id) FROM avaliacoes));
