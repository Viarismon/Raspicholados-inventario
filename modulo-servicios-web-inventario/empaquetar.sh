#!/usr/bin/env bash
# GA8-AA1-EV02: genera el paquete ejecutable dist/raspicholados-inventario-v1.1.0.zip
# (JAR compilado para Java 8 + interfaz web + script SQL + configuracion de ejemplo)
set -e
cd "$(dirname "$0")"
V=1.1.0; P=dist/raspicholados-inventario-v$V
rm -rf bin-dist "$P" "$P.zip" && mkdir -p bin-dist "$P/lib" "$P/docs" "$P/sql"
javac --release 8 -nowarn -encoding UTF-8 -d bin-dist $(find src -name "*.java")
jar cfe "$P/raspicholados-inventario.jar" co.sena.raspicholados.api.Main -C bin-dist .
cp -r web "$P/" && cp docs/openapi.json "$P/docs/" && cp sql/raspicholados_api_v2.sql "$P/sql/"
cp config.properties config.local.ejemplo.properties "$P/"
cp empaque/LEEME.txt empaque/iniciar.bat empaque/iniciar.sh "$P/"
echo "Copie aqui mysql-connector-j-x.x.x.jar" > "$P/lib/COPIE_AQUI_EL_DRIVER.txt"
(cd dist && rm -f "raspicholados-inventario-v$V.zip" && \
  if command -v zip >/dev/null; then zip -qr "raspicholados-inventario-v$V.zip" "raspicholados-inventario-v$V"; \
  else jar cfM "raspicholados-inventario-v$V.zip" "raspicholados-inventario-v$V"; fi)
rm -rf bin-dist
echo "Paquete listo: $P.zip"
