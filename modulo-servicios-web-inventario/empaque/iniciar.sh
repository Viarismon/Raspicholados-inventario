#!/usr/bin/env bash
# Inicia la aplicacion de inventario Raspicholados desde el paquete ejecutable
cd "$(dirname "$0")"
SEP=":"; case "$(uname -s)" in MINGW*|CYGWIN*|MSYS*) SEP=";";; esac
java -Dfile.encoding=UTF-8 -cp "raspicholados-inventario.jar${SEP}lib/*" co.sena.raspicholados.api.Main config.properties
