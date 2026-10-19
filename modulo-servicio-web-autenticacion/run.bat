@echo off
REM Ejecuta el servicio web ya compilado en Windows.
REM Debe ejecutarse build.bat primero.

java -cp bin com.raspicholados.auth.Main
pause
