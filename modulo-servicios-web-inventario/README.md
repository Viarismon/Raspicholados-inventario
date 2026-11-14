# API REST de inventario — Raspicholados

**Evidencia:** GA7-220501096-AA5-EV03 — Diseño y desarrollo de servicios web, proyecto
**Aprendiz:** Viviana Arias Montilla · Tecnología en Análisis y Desarrollo de Software (SENA)
**Versión:** 1.0.0

Servicios web REST que cubren las funciones del software de inventario de la heladería
Raspicholados: autenticación con token, usuarios con roles, categorías, proveedores,
productos, movimientos de inventario (entradas, salidas y ajustes), alertas de stock
bajo y resumen gerencial. En total son **28 servicios**.

La documentación completa de cada servicio está en [`docs/API.md`](docs/API.md) y el
contrato formal en [`docs/openapi.json`](docs/openapi.json) (OpenAPI 3.0.3).

## 1. Servicios

| Recurso | Servicios |
|---|---|
| Sistema | `GET /api`, `GET /api/salud`, `GET /api/docs` |
| Autenticación | `POST /api/auth/login`, `GET /api/auth/perfil`, `PUT /api/auth/contrasena` |
| Usuarios | `POST /api/usuarios`, `GET /api/usuarios` |
| Categorías | `GET`, `POST /api/categorias` · `GET`, `PUT`, `DELETE /api/categorias/{id}` |
| Proveedores | `GET`, `POST /api/proveedores` · `GET`, `PUT`, `DELETE /api/proveedores/{id}` |
| Productos | `GET`, `POST /api/productos` · `GET`, `PUT`, `DELETE /api/productos/{id}` |
| Movimientos | `GET`, `POST /api/movimientos` · `GET /api/movimientos/{id}` |
| Reportes | `GET /api/alertas/stock-bajo`, `GET /api/reportes/resumen` |

## 2. Tecnología y arquitectura

- **Java 8 o superior**, sin Maven ni frameworks: servidor `com.sun.net.httpserver` del JDK.
- **MySQL** con JDBC (driver MySQL Connector/J en `lib/`). Si MySQL no está disponible,
  la API puede trabajar **en memoria** con datos de ejemplo (`persistencia.modo=memoria`).
- **Seguridad:** token JWT HS256, contraseñas con PBKDF2-HMAC-SHA256 (210 000 iteraciones),
  bloqueo tras 5 intentos fallidos, roles ADMINISTRADOR y EMPLEADO.
- **Capas:** `http` (enrutador) → `controlador` → `servicio` (reglas de negocio) →
  `repositorio` (interfaces) → `memoria` | `mysql`.

```
modulo-servicios-web-inventario/
├── src/co/sena/raspicholados/api/
│   ├── Main.java                 # Ensambla capas y arranca el servidor
│   ├── config/                   # Lectura de config.properties
│   ├── http/                     # Router, Solicitud, Respuesta, ApiException
│   ├── json/                     # Parser y serializador JSON propio (RFC 8259)
│   ├── seguridad/                # JWT, PBKDF2, control de intentos
│   ├── modelo/                   # Usuario, Categoria, Proveedor, Producto, Movimiento
│   ├── repositorio/              # Interfaces + implementaciones memoria/ y mysql/
│   ├── servicio/                 # Reglas de negocio y validaciones
│   ├── controlador/              # Un controlador por recurso
│   └── util/                     # Fechas
├── sql/raspicholados_api_v2.sql  # Modelo de datos y datos de ejemplo
├── docs/API.md · docs/openapi.json
├── postman/                      # Colección (67 pruebas) y entorno
├── pruebas/reporte_newman_aa5_ev03.html
├── config.properties             # Configuración (puerto, BD, JWT, admin inicial)
└── build.bat · run.bat · build.sh · run.sh
```

## 3. Cómo ejecutarlo en Windows

1. **Base de datos.** En MySQL Workbench o phpMyAdmin (XAMPP) ejecute
   `sql/raspicholados_api_v2.sql`. Ojo: el script recrea las tablas del inventario.
2. **Driver JDBC.** Copie `mysql-connector-j-x.x.x.jar` en la carpeta `lib\`.
3. **Configuración.** Revise usuario y contraseña de MySQL en `config.properties`.
   Para una prueba rápida sin MySQL cambie a `persistencia.modo=memoria`.
4. **Compilar y ejecutar.** Doble clic en `build.bat` y luego en `run.bat`.
   La consola debe mostrar `URL base : http://localhost:8080/api`.
5. **Probar.** En Postman importe los dos archivos de `postman\`, seleccione el entorno
   *Raspicholados Inventario Local* y use *Run collection*.

Usuario inicial: `admin` / `Raspi2026*` (lo crea la API si la tabla `usuario` está vacía;
cámbielo con `PUT /api/auth/contrasena`).

## 4. Ejemplo rápido (curl)

```bash
# 1. Iniciar sesión y copiar el token
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" \
     -d "{\"usuario\":\"admin\",\"contrasena\":\"Raspi2026*\"}"

# 2. Registrar la venta de 5 kg de hielo
curl -X POST http://localhost:8080/api/movimientos -H "Content-Type: application/json" \
     -H "Authorization: Bearer <token>" \
     -d "{\"productoId\":1,\"tipo\":\"SALIDA\",\"cantidad\":5,\"motivo\":\"Ventas de la tarde\"}"
```

## 5. Hallazgos de la AA5-EV02 corregidos en esta versión

| Hallazgo | Corrección |
|---|---|
| H-01 la clave se cortaba en una comilla escapada | Parser JSON completo propio (`json/Json.java`) |
| H-02 un usuario con `\|` se perdía al reiniciar | Usuarios en la tabla `usuario` de MySQL y nombre restringido a letras, números, `.`, `-` y `_` |
| H-03 sin límite de intentos | `ControlIntentos`: 5 fallos → bloqueo de 15 min (429) |
| H-04 claves de 1 carácter | Política: 8 a 64 caracteres, con letra y número |
| H-05 `/api/loginXYZ` respondía como `/api/login` | Enrutador con coincidencia exacta de rutas |
| H-06 mismo 400 para JSON malformado y datos faltantes | Códigos `JSON_MALFORMADO` y `VALIDACION_FALLIDA` |
| H-07 405 sin cabecera `Allow` | El enrutador la agrega siempre |
| H-08 404 en HTML | Todos los errores son JSON |
| Login sin token | JWT HS256 con vigencia de 120 min |
| SHA-256 simple | PBKDF2-HMAC-SHA256 con 210 000 iteraciones |
