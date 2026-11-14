#!/usr/bin/env bash
# Ejecuta la API (Git Bash, Linux o macOS)
cd "$(dirname "$0")"
SEP=":"; case "$(uname -s)" in MINGW*|CYGWIN*|MSYS*) SEP=";";; esac
java -Dfile.encoding=UTF-8 -cp "bin${SEP}lib/*" co.sena.raspicholados.api.Main config.properties
