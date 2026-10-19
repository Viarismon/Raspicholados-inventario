@echo off
REM Compila el servicio web en Windows (CMD o doble clic).
REM Requiere el JDK 8 o superior instalado (comando javac disponible).

if not exist bin mkdir bin
dir /s /b src\*.java > fuentes.txt
javac -encoding UTF-8 -d bin @fuentes.txt
if errorlevel 1 (
  echo La compilacion fallo. Revise los errores anteriores.
  del fuentes.txt
  pause
  exit /b 1
)
del fuentes.txt
echo Compilacion exitosa. Clases generadas en la carpeta bin\
pause
