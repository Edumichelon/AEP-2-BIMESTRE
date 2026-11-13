-- Cria o usuário e o banco do projeto num PostgreSQL instalado.
-- Rodar como superusuário (postgres) pelo psql. Pode rodar de novo: não recria o que já existe.
SELECT 'CREATE ROLE marido LOGIN PASSWORD ''marido123'''
WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'marido')\gexec

SELECT 'CREATE DATABASE marido_aluguel OWNER marido ENCODING ''UTF8'' TEMPLATE template0'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'marido_aluguel')\gexec
