@echo off
REM Ejecuta la API. Si existe un driver JDBC en lib\ se agrega al classpath.
REM Descargue mysql-connector-j-x.x.x.jar y copielo en la carpeta lib\
cd /d "%~dp0"
chcp 65001 > nul
java -Dfile.encoding=UTF-8 -cp "bin;lib\*" co.sena.raspicholados.api.Main config.properties
pause
