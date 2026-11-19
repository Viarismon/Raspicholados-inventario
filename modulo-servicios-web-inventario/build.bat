@echo off
REM ==========================================================================
REM  Compila la aplicacion web de inventario (Windows: CMD o doble clic).
REM  Requiere JDK 8 o superior (comandos javac y jar disponibles).
REM  Resultado:  bin\                               clases compiladas
REM              dist\raspicholados-inventario.jar  archivo compilado (JAR)
REM ==========================================================================
cd /d "%~dp0"
if exist bin rmdir /s /q bin
mkdir bin
dir /s /b src\*.java > fuentes.txt
javac -encoding UTF-8 -d bin @fuentes.txt
if errorlevel 1 (
  echo.
  echo La compilacion fallo. Revise los errores anteriores.
  del fuentes.txt
  pause
  exit /b 1
)
del fuentes.txt
if not exist dist mkdir dist
jar cfe dist\raspicholados-inventario.jar co.sena.raspicholados.api.Main -C bin .
echo.
echo Compilacion exitosa:
echo   - Clases en bin\
echo   - Archivo compilado en dist\raspicholados-inventario.jar
pause
