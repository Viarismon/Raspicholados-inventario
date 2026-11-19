#!/usr/bin/env bash
# Compila la aplicación (Git Bash, Linux o macOS) y genera dist/raspicholados-inventario.jar
set -e
cd "$(dirname "$0")"
rm -rf bin && mkdir -p bin dist
javac -encoding UTF-8 -d bin $(find src -name "*.java")
jar cfe dist/raspicholados-inventario.jar co.sena.raspicholados.api.Main -C bin .
echo "Compilación exitosa: bin/ y dist/raspicholados-inventario.jar"
