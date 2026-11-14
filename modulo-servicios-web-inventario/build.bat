@echo off
REM Compila la API de inventario en Windows (CMD o doble clic).
REM Requiere JDK 8 o superior (comando javac disponible).
cd /d "%~dp0"
if not exist bin mkdir bin
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
echo.
echo Compilacion exitosa. Clases generadas en bin\
pause
