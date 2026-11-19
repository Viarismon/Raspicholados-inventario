#!/usr/bin/env bash
# Ejecuta las pruebas unitarias JUnit 4 (Git Bash, Linux o macOS)
set -e
cd "$(dirname "$0")"
SEP=":"; case "$(uname -s)" in MINGW*|CYGWIN*|MSYS*) SEP=";";; esac
rm -rf bin-test && mkdir -p bin-test
javac -encoding UTF-8 -d bin-test -cp "lib/test/*" $(find src test -name "*.java")
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp "bin-test${SEP}lib/test/*" org.junit.runner.JUnitCore co.sena.raspicholados.api.TodasLasPruebas | tee pruebas/resultado_pruebas_unitarias.txt
