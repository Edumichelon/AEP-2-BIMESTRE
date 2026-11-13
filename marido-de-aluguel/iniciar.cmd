@echo off
rem Marido de Aluguel - inicia o sistema sem instalar nada (so precisa do Java 21+).
rem Usa o perfil "local": um PostgreSQL real embutido, recriado com database\schema.sql e seed.sql a cada execucao.
setlocal
chcp 65001 >nul
title Marido de Aluguel
cd /d "%~dp0"

where java >nul 2>nul || (
    echo Java nao encontrado. Instale o Java 21 ou mais novo: https://adoptium.net
    pause
    exit /b 1
)

echo.
echo  Marido de Aluguel
echo  -----------------
echo  Iniciando... A primeira vez demora alguns minutos (baixa as dependencias).
echo  O navegador abre sozinho em http://localhost:8080 quando estiver pronto.
echo  Para encerrar: feche esta janela ou aperte Ctrl+C.
echo.

call "%~dp0backend\abrir-navegador.cmd"
cd /d "%~dp0backend"
set "SPRING_PROFILES_ACTIVE=local"
call "%~dp0backend\mvnw.cmd" spring-boot:run
pause
