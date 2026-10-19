#!/bin/bash
# Compila el proyecto sin necesidad de Maven ni Gradle.
# Requiere tener instalado el JDK (javac) version 8 o superior.

mkdir -p bin
javac -encoding UTF-8 -d bin $(find src -name "*.java")

if [ $? -eq 0 ]; then
  echo "Compilación exitosa. Clases generadas en la carpeta bin/"
else
  echo "La compilación falló. Revise los errores anteriores."
  exit 1
fi
