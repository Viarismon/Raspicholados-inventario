@echo off
REM ==========================================================================
REM  GA8-AA1-EV02: genera el paquete ejecutable
REM  dist\raspicholados-inventario-v1.1.0.zip (JAR Java 8 + web + SQL + config)
REM ==========================================================================
cd /d "%~dp0"
set V=1.1.0
set P=dist\raspicholados-inventario-v%V%
if exist bin-dist rmdir /s /q bin-dist
if exist %P% rmdir /s /q %P%
if exist %P%.zip del %P%.zip
mkdir bin-dist
mkdir %P%\lib %P%\docs %P%\sql %P%\web
dir /s /b src\*.java > fuentes.txt
javac --release 8 -nowarn -encoding UTF-8 -d bin-dist @fuentes.txt
if errorlevel 1 (
  echo La compilacion fallo.
  del fuentes.txt
  pause
  exit /b 1
)
del fuentes.txt
jar cfe %P%\raspicholados-inventario.jar co.sena.raspicholados.api.Main -C bin-dist .
xcopy /e /i /q web %P%\web > nul
copy docs\openapi.json %P%\docs\ > nul
copy sql\raspicholados_api_v2.sql %P%\sql\ > nul
copy config.properties %P%\ > nul
copy config.local.ejemplo.properties %P%\ > nul
copy empaque\LEEME.txt %P%\ > nul
copy empaque\iniciar.bat %P%\ > nul
copy empaque\iniciar.sh %P%\ > nul
echo Copie aqui mysql-connector-j-x.x.x.jar > %P%\lib\COPIE_AQUI_EL_DRIVER.txt
cd dist
jar cfM raspicholados-inventario-v%V%.zip raspicholados-inventario-v%V%
cd ..
rmdir /s /q bin-dist
echo.
echo Paquete listo: %P%.zip
pause
