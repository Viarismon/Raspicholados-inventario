@echo off
REM ==========================================================================
REM  Ejecuta las pruebas unitarias (JUnit 4.13.2) de todos los modulos.
REM  Las librerias de prueba estan en lib\test\ (junit y hamcrest).
REM  El resultado tambien se guarda en pruebas\resultado_pruebas_unitarias.txt
REM ==========================================================================
cd /d "%~dp0"
chcp 65001 > nul
if exist bin-test rmdir /s /q bin-test
mkdir bin-test
dir /s /b src\*.java test\*.java > fuentes-test.txt
javac -encoding UTF-8 -d bin-test -cp "lib\test\*" @fuentes-test.txt
if errorlevel 1 (
  echo La compilacion de las pruebas fallo.
  del fuentes-test.txt
  pause
  exit /b 1
)
del fuentes-test.txt
java -Dfile.encoding=UTF-8 -cp "bin-test;lib\test\*" org.junit.runner.JUnitCore co.sena.raspicholados.api.TodasLasPruebas > pruebas\resultado_pruebas_unitarias.txt 2>&1
type pruebas\resultado_pruebas_unitarias.txt
pause
