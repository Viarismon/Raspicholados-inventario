#!/usr/bin/env bash
# Compila la API (Git Bash, Linux o macOS)
cd "$(dirname "$0")"
mkdir -p bin
javac -encoding UTF-8 -d bin $(find src -name "*.java") && echo "Compilación exitosa (bin/)"
