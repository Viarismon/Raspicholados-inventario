# Manual técnico — Sistema de inventario Raspicholados v1.1.0

**Evidencia:** GA8-220501096-AA1-EV02 — Módulos integrados
**Aprendiz:** Viviana Arias Montilla · Tecnología en Análisis y Desarrollo de Software (SENA)
**Instructor:** William Fernando Muñoz
**Fecha:** noviembre de 2026

Este manual está dirigido a quien instala, configura, prueba o mantiene la aplicación. El
manual de uso de las pantallas está en el informe de la evidencia; aquí se tratan la
arquitectura, los ambientes, la configuración del servidor y de la base de datos, la
compilación, el despliegue y la solución de problemas.

---

## 1. Visión general

La aplicación integrada es un único proceso Java que publica dos cosas en el mismo puerto:

| Ruta | Qué entrega | Componente |
|---|---|---|
| `http://localhost:8080/` | Interfaz web (HTML, CSS, JavaScript) | `web/` servido por `http/ManejadorEstatico` |
| `http://localhost:8080/api/...` | 28 servicios REST en JSON | `http/Router` → controladores → servicios → repositorios |

La interfaz consume la API con `fetch` y un token JWT. La API guarda los datos en MySQL
(`raspicholados_db`) o, si MySQL no responde y así se configuró, en memoria con datos de
ejemplo.

```
Navegador ──HTTP──► Servidor JDK (com.sun.net.httpserver, puerto 8080)
                      ├─ "/"    ManejadorEstatico ──► web/index.html, css/, js/
                      └─ "/api" Router ──► Controlador ──► Servicio ──► Repositorio
                                                                         ├─ mysql/  ──JDBC──► MySQL 5.7/8
                                                                         └─ memoria/ (respaldo)
```

## 2. Componentes y responsabilidades

| Paquete / carpeta | Responsabilidad | Depende de |
|---|---|---|
| `Main` | Lee la configuración, elige la persistencia, ensambla las capas y arranca el servidor | todos |
| `config` | `Configuracion`: une `config.properties`, `config.local.properties` y variables `RASPI_*` | — |
| `http` | `Router`, `Ruta`, `Solicitud`, `Respuesta`, `ApiException`, `Seguridad`, `ManejadorEstatico` | `json`, `seguridad` |
| `json` | Analizador y serializador JSON propio (RFC 8259) | — |
| `seguridad` | `JwtUtil` (HS256), `ContrasenaUtil` (PBKDF2), `ControlIntentos` | `http` (excepciones) |
| `modelo` | `Usuario`, `Categoria`, `Proveedor`, `Producto`, `Movimiento` | `json` |
| `repositorio` | Interfaces + `memoria/` + `mysql/` | `modelo` |
| `servicio` | Reglas de negocio y validaciones (`Validador`) | `repositorio`, `seguridad` |
| `controlador` | Traduce HTTP ↔ servicio, uno por recurso | `servicio`, `http` |
| `web/` | Interfaz: `js/api.js` (cliente HTTP), `js/app.js` (vistas), `css/styles.css` | API REST |

## 3. Ambientes

| Aspecto | Desarrollo | Pruebas |
|---|---|---|
| Equipo | Portátil ACER, Windows 10 | Mismo portátil (pruebas de integración y Postman) y contenedor Linux (Ubuntu, pruebas automáticas) |
| JDK | 25.0.4.1 (código compatible con Java 8) | JDK 21 en Linux, JDK 25 en Windows |
| Base de datos | MySQL 5.7 + Workbench 8.0 CE, usuario `root` | MySQL 5.7 (Windows) y modo `memoria` (Linux) |
| Persistencia | `mysql` con respaldo en memoria | `memoria` para pruebas unitarias; `mysql` para Newman en Windows |
| Editor y control de versiones | VS Code, Git Bash, GitHub | — |
| Herramientas de prueba | — | JUnit 4.13.2 + Hamcrest 1.3 (`test.bat`), Postman 12 y Newman 6.2 |
| Navegador | Chrome | Chrome (y Chrome sin interfaz para capturas) |

## 4. Requisitos de instalación

- **Java 8 o superior** (`java -version`). Para compilar se necesita el JDK (`javac`, `jar`).
- **MySQL 5.7 u 8.0** (opcional en pruebas: sin MySQL la aplicación pasa a modo memoria).
- **MySQL Connector/J** (`mysql-connector-j-x.x.x.jar`) copiado en `lib/`. No se versiona.
- Puerto **8080** libre (o cambiar `servidor.puerto`).
- Navegador moderno (Chrome, Edge o Firefox).

## 5. Configuración del servidor

La configuración se lee en este orden; cada fuente reemplaza a la anterior:

1. `config.properties` (versionado, sin contraseñas reales).
2. `config.local.properties` (ignorado por Git, contraseñas del equipo).
3. Variables de entorno `RASPI_<CLAVE>` (por ejemplo `RASPI_BD_CONTRASENA`).

| Clave | Valor por defecto | Descripción |
|---|---|---|
| `servidor.puerto` | `8080` | Puerto HTTP |
| `web.directorio` | `web` | Carpeta de la interfaz web |
| `cors.origen` | vacío (deshabilitado) | Origen permitido si la interfaz se sirve desde otro dominio |
| `persistencia.modo` | `mysql` | `mysql` o `memoria` |
| `persistencia.respaldo.memoria` | `true` | Si MySQL falla, continuar en memoria |
| `bd.url` | `jdbc:mysql://localhost:3306/raspicholados_db?useSSL=false&serverTimezone=America/Bogota&allowPublicKeyRetrieval=true` | Cadena JDBC |
| `bd.usuario` | `root` | Usuario de MySQL |
| `bd.contrasena` | vacío | Contraseña (va en `config.local.properties`) |
| `jwt.secreto` | texto de ejemplo | Secreto HS256, mínimo 32 caracteres. **Cambiarlo en cada instalación** |
| `jwt.minutos.vigencia` | `120` | Vigencia del token |
| `admin.usuario` / `admin.contrasena.inicial` | `admin` / `Raspi2026*` | Se crea solo si la tabla `usuario` está vacía |

Cabeceras de seguridad que agrega el servidor a la interfaz: `Content-Security-Policy`
(sin `unsafe-inline`), `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`.
Las rutas con `..` se rechazan.

## 6. Configuración de la base de datos

- Esquema: `raspicholados_db`, juego de caracteres `utf8mb4`, intercalación `utf8mb4_spanish_ci`, motor InnoDB.
- Script: `sql/raspicholados_api_v2.sql` (crea la base, las cinco tablas y datos de ejemplo). **Recrea las tablas**: no ejecutarlo sobre datos reales.

| Tabla | Llave | Relaciones y restricciones |
|---|---|---|
| `usuario` | `id_usuario` | `nombre_usuario` único; `rol` ENUM(ADMINISTRADOR, EMPLEADO); hash PBKDF2 |
| `categoria` | `id_categoria` | `nombre` único |
| `proveedor` | `id_proveedor` | `nit` único |
| `producto` | `id_producto` | FK `categoria` → `categoria.nombre` (ON UPDATE CASCADE); FK `id_proveedor` (ON DELETE SET NULL); CHECK de stock y precio |
| `movimiento_inventario` | `id_movimiento` | FK a producto y usuario; tipo ENTRADA/SALIDA/AJUSTE; guarda stock anterior y resultante; índices por fecha y producto |

**Nota:** MySQL 5.7 acepta las cláusulas `CHECK` pero no las aplica (se aplican desde
MySQL 8.0.16). En 5.7 esas reglas las garantiza la capa de servicio (`Validador`) y el
registro de movimientos, que lee, valida y actualiza el stock en una sola transacción con
bloqueo de fila (`SELECT ... FOR UPDATE`).

**Usuario recomendado para producción** (en lugar de `root`):

```sql
CREATE USER 'raspi_app'@'localhost' IDENTIFIED BY 'una-clave-larga-y-propia';
GRANT SELECT, INSERT, UPDATE, DELETE ON raspicholados_db.* TO 'raspi_app'@'localhost';
FLUSH PRIVILEGES;
```

Luego `bd.usuario=raspi_app` y la clave en `config.local.properties`.

**Copia de seguridad y restauración:**

```bash
mysqldump -u root -p --routines raspicholados_db > respaldo_raspicholados.sql
mysql -u root -p raspicholados_db < respaldo_raspicholados.sql
```

## 7. Compilación, pruebas y empaquetado

| Script (Windows / Bash) | Resultado |
|---|---|
| `build.bat` / `build.sh` | Clases en `bin/` y `dist/raspicholados-inventario.jar` |
| `run.bat` / `run.sh` | Ejecuta desde `bin/` |
| `run-jar.bat` | Ejecuta desde el JAR |
| `test.bat` / `test.sh` | 65 pruebas JUnit; salida en `pruebas/resultado_pruebas_unitarias.txt` |
| `empaquetar.bat` / `empaquetar.sh` | `dist/raspicholados-inventario-v1.1.0.zip`: JAR compilado para Java 8, `web/`, SQL, configuración y `iniciar.bat` |

Pruebas de la API: importar `postman/Raspicholados_API_Inventario.postman_collection.json` y el
entorno `Raspicholados_Inventario_Local`, y ejecutar *Run collection*; o por consola:

```bash
newman run postman/Raspicholados_API_Inventario.postman_collection.json \
  -e postman/Raspicholados_Inventario_Local.postman_environment.json
```

Resultado esperado: 71 peticiones, 205 aserciones, 0 fallos.

## 8. Despliegue

### 8.1 Despliegue local (aplicación integrada)

1. Ejecutar `sql/raspicholados_api_v2.sql` en MySQL Workbench (rayo ⚡).
2. Copiar el driver a `lib/` y crear `config.local.properties` con la contraseña.
3. `build.bat` y luego `run.bat` (o descomprimir el paquete y usar `iniciar.bat`).
4. Comprobar en la consola: `[BD] Conexión con MySQL verificada` y `Aplicación : http://localhost:8080/`.
5. Comprobar `http://localhost:8080/api/salud` → `"estado": "OK"`, `"persistencia": "mysql"`.

### 8.2 Despliegue en GitHub Pages (módulos estáticos)

La rama `gh-pages` publica en `https://viarismon.github.io/Raspicholados-inventario/`:
el prototipo front-end (`/prototipo/`), el contrato OpenAPI (`/api/`), los reportes de
prueba (`/pruebas/`) y el paquete ejecutable (`/descargas/`).
Activación: GitHub → *Settings* → *Pages* → *Source: Deploy from a branch* → `gh-pages` / `(root)` → *Save*.

GitHub Pages solo sirve archivos estáticos: no ejecuta Java ni MySQL. Por eso la
aplicación integrada no se publica allí (ver la propuesta de despliegue en la nube en el informe).

## 9. Solución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| `No suitable driver found for jdbc:mysql` | Falta el driver en `lib/` | Copiar `mysql-connector-j-x.x.x.jar` a `lib/` |
| `Access denied for user 'root'` | Contraseña errónea | Revisar `config.local.properties` |
| `Unknown database 'raspicholados_db'` | No se ejecutó el script | Ejecutar `sql/raspicholados_api_v2.sql` |
| `Address already in use` / `BindException` | Puerto 8080 ocupado | Cerrar el otro proceso o cambiar `servidor.puerto` |
| La consola dice `Persistencia : memoria (respaldo...)` | MySQL apagado o sin driver | Iniciar MySQL; revisar los dos puntos anteriores |
| Login responde 429 | 5 intentos fallidos | Esperar 15 minutos o reiniciar el servidor |
| Login tarda 1 a 3 segundos | PBKDF2 con 210 000 iteraciones | Comportamiento esperado |
| `Unlink of file ... failed (y/n)` al usar Git | Archivo bloqueado por Windows | Responder `n` |

## 10. Mantenimiento

- Ramas: `main` (estable), `feature/<evidencia>` para cada cambio, `gh-pages` (sitio).
- Etiquetas por entrega: `v1.0.0-aa5-ev03`, `v1.1.0-ga8`, `v1.1.1-ga8-ev02`.
- Antes de subir cambios: `test.bat` en verde y la colección de Postman sin fallos.
- Cambiar `jwt.secreto` y la contraseña del administrador en cada instalación nueva.
