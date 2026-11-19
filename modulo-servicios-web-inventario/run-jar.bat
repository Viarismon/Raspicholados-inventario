@echo off
REM Ejecuta la aplicacion desde el archivo compilado dist\raspicholados-inventario.jar
cd /d "%~dp0"
chcp 65001 > nul
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp "dist\raspicholados-inventario.jar;lib\*" co.sena.raspicholados.api.Main config.properties
pause
