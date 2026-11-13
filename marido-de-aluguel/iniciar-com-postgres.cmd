@echo off
rem Marido de Aluguel - inicia o sistema usando o PostgreSQL instalado no computador.
rem Na primeira vez cria o usuario "marido", o banco "marido_aluguel" e carrega schema.sql e seed.sql.
rem Os dados ficam salvos entre uma execucao e outra.
setlocal
chcp 65001 >nul
title Marido de Aluguel (PostgreSQL instalado)
cd /d "%~dp0"

where java >nul 2>nul || (
    echo Java nao encontrado. Instale o Java 21 ou mais novo: https://adoptium.net
    pause
    exit /b 1
)

rem Procura o psql da instalacao do PostgreSQL
set "PSQL="
for /d %%D in ("%ProgramFiles%\PostgreSQL\*") do if exist "%%D\bin\psql.exe" set "PSQL=%%D\bin\psql.exe"
if not defined PSQL for /f "delims=" %%P in ('where psql 2^>nul') do set "PSQL=%%P"
if not defined PSQL (
    echo PostgreSQL nao encontrado.
    echo Instale em https://www.postgresql.org/download/windows/ ou use o iniciar.cmd, que nao precisa de instalacao.
    pause
    exit /b 1
)

set "PGCLIENTENCODING=UTF8"
set "PGPASSWORD=marido123"
"%PSQL%" -h localhost -U marido -d marido_aluguel -tAc "SELECT 1 FROM usuarios LIMIT 1" >nul 2>nul
if errorlevel 1 (
    call :prepararBanco
    if errorlevel 1 (
        echo.
        echo Nao foi possivel preparar o banco. Confira se o PostgreSQL esta rodando e a senha do usuario postgres.
        pause
        exit /b 1
    )
)

echo.
echo  Marido de Aluguel - banco marido_aluguel no PostgreSQL instalado
echo  O navegador abre sozinho em http://localhost:8080 quando estiver pronto.
echo  Para encerrar: feche esta janela ou aperte Ctrl+C.
echo.

call "%~dp0backend\abrir-navegador.cmd"
cd /d "%~dp0backend"
set "SPRING_PROFILES_ACTIVE="
set "PGPASSWORD="
call "%~dp0backend\mvnw.cmd" spring-boot:run
pause
exit /b 0

:prepararBanco
echo.
echo Primeira execucao: vou criar o banco marido_aluguel.
echo Digite a senha do usuario "postgres" que voce definiu na instalacao do PostgreSQL.
set "PGPASSWORD="
"%PSQL%" -h localhost -U postgres -d postgres -v ON_ERROR_STOP=1 -q -f "%~dp0database\criar-banco.sql"
if errorlevel 1 exit /b 1
set "PGPASSWORD=marido123"
echo Criando tabelas (schema.sql)...
"%PSQL%" -h localhost -U marido -d marido_aluguel -v ON_ERROR_STOP=1 -q -f "%~dp0database\schema.sql"
if errorlevel 1 exit /b 1
echo Carregando dados de exemplo (seed.sql)...
"%PSQL%" -h localhost -U marido -d marido_aluguel -v ON_ERROR_STOP=1 -q -f "%~dp0database\seed.sql" >nul
if errorlevel 1 exit /b 1
echo Banco pronto.
exit /b 0
