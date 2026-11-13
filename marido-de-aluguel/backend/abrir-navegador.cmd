@echo off
rem Abre http://localhost:8080 no navegador assim que a API comecar a responder (roda em segundo plano).
if defined MDA_SEM_NAVEGADOR exit /b 0
start "" /b powershell -NoProfile -ExecutionPolicy Bypass -Command "for ($i = 0; $i -lt 300; $i++) { try { (New-Object Net.Sockets.TcpClient('localhost', 8080)).Close(); Start-Process 'http://localhost:8080'; break } catch { Start-Sleep -Seconds 2 } }"
exit /b 0
