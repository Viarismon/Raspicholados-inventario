@echo off
REM Inicia la aplicacion de inventario Raspicholados desde el paquete ejecutable
cd /d "%~dp0"
chcp 65001 > nul
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp "raspicholados-inventario.jar;lib\*" co.sena.raspicholados.api.Main config.properties
pause
