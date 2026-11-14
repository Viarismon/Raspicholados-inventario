# Documentación de los servicios web — API de inventario Raspicholados

**Evidencia:** GA7-220501096-AA5-EV03 — Diseño y desarrollo de servicios web, proyecto
**Aprendiz:** Viviana Arias Montilla · Tecnología en Análisis y Desarrollo de Software (SENA)
**Versión de la API:** 1.0.0 · **URL base:** `http://localhost:8080/api`
**Contrato formal:** [`openapi.json`](openapi.json) (OpenAPI 3.0.3), también disponible en `GET /api/docs`

## 1. Convenciones generales

| Aspecto | Convención |
|---|---|
| Formato | JSON en UTF-8. Las peticiones con cuerpo (POST, PUT) exigen `Content-Type: application/json` (si no, 415). |
| Autenticación | Token JWT (HS256) obtenido en `POST /api/auth/login`. Se envía en cada petición: `Authorization: Bearer <token>`. Vigencia: 120 minutos. |
| Roles | `ADMINISTRADOR` (gestiona catálogos y usuarios) y `EMPLEADO` (consulta y registra movimientos). |
| Respuesta exitosa | Un recurso: `{ "datos": {...} }`. Una colección: `{ "datos": [...], "total": n }`. Creación: 201 con `mensaje`, `datos` y cabecera `Location`. |
| Respuesta de error | `{ "error": { "estado", "codigo", "mensaje", "detalles"?, "ruta", "fecha" } }`. `codigo` es estable y legible por programas. |
| Fechas | ISO 8601 local: `2026-11-14T08:30:00`. Filtros de fecha: `AAAA-MM-DD`. |
| Dinero | Número con máximo 2 decimales (pesos colombianos). |
| CORS | Habilitado (`Access-Control-Allow-Origin: *`) para que el front-end consuma la API. |

### 1.1 Códigos de estado y códigos de error

| HTTP | `codigo` | Cuándo ocurre |
|---|---|---|
| 400 | `JSON_MALFORMADO`, `CUERPO_VACIO`, `JSON_NO_ES_OBJETO` | El cuerpo no es un objeto JSON válido |
| 400 | `VALIDACION_FALLIDA` | Uno o más campos no cumplen las reglas; `detalles` trae un error por campo |
| 400 | `PARAMETRO_INVALIDO` | Parámetro de ruta o de consulta con formato incorrecto |
| 401 | `TOKEN_REQUERIDO`, `TOKEN_INVALIDO`, `TOKEN_EXPIRADO` | Problemas con el token |
| 401 | `CREDENCIALES_INVALIDAS` | Usuario o contraseña incorrectos |
| 403 | `PERMISO_DENEGADO` | El rol del token no puede hacer la operación |
| 404 | `RECURSO_NO_ENCONTRADO`, `RUTA_NO_ENCONTRADA` | El id o la ruta no existen |
| 405 | `METODO_NO_PERMITIDO` | Método no soportado en esa ruta (incluye cabecera `Allow`) |
| 409 | `*_DUPLICADO(A)`, `*_CON_PRODUCTOS`, `STOCK_INSUFICIENTE`, `PRODUCTO_INACTIVO` | Conflicto con una regla de negocio |
| 413 | `CUERPO_DEMASIADO_GRANDE` | Cuerpo mayor a 1 MB |
| 415 | `TIPO_CONTENIDO_NO_SOPORTADO` | Falta `Content-Type: application/json` |
| 429 | `CUENTA_BLOQUEADA` | 5 intentos de login fallidos (bloqueo de 15 min, cabecera `Retry-After`) |
| 500 / 503 | `ERROR_INTERNO`, `BASE_DATOS_NO_DISPONIBLE` | Falla del servidor o de MySQL (sin exponer detalles internos) |

## 2. Catálogo de servicios

| # | Método | Ruta | Acceso | Descripción |
|---|---|---|---|---|
| SIS-01 | `GET` | `/api` | PUBLICA | Índice de rutas |
| SIS-02 | `GET` | `/api/salud` | PUBLICA | Estado del servicio |
| SIS-03 | `GET` | `/api/docs` | PUBLICA | Especificación OpenAPI |
| AUT-01 | `POST` | `/api/auth/login` | PUBLICA | Iniciar sesión |
| AUT-02 | `GET` | `/api/auth/perfil` | AUTENTICADA | Perfil del usuario autenticado |
| AUT-03 | `PUT` | `/api/auth/contrasena` | AUTENTICADA | Cambiar la propia contraseña |
| USU-01 | `POST` | `/api/usuarios` | ADMINISTRADOR | Registrar usuario |
| USU-02 | `GET` | `/api/usuarios` | ADMINISTRADOR | Listar usuarios |
| CAT-01 | `GET` | `/api/categorias` | AUTENTICADA | Listar categorías |
| CAT-02 | `GET` | `/api/categorias/{id}` | AUTENTICADA | Consultar categoría |
| CAT-03 | `POST` | `/api/categorias` | ADMINISTRADOR | Crear categoría |
| CAT-04 | `PUT` | `/api/categorias/{id}` | ADMINISTRADOR | Actualizar categoría |
| CAT-05 | `DELETE` | `/api/categorias/{id}` | ADMINISTRADOR | Eliminar categoría |
| PRV-01 | `GET` | `/api/proveedores` | AUTENTICADA | Listar proveedores |
| PRV-02 | `GET` | `/api/proveedores/{id}` | AUTENTICADA | Consultar proveedor |
| PRV-03 | `POST` | `/api/proveedores` | ADMINISTRADOR | Crear proveedor |
| PRV-04 | `PUT` | `/api/proveedores/{id}` | ADMINISTRADOR | Actualizar proveedor |
| PRV-05 | `DELETE` | `/api/proveedores/{id}` | ADMINISTRADOR | Eliminar proveedor |
| PRO-01 | `GET` | `/api/productos` | AUTENTICADA | Listar y filtrar productos |
| PRO-02 | `GET` | `/api/productos/{id}` | AUTENTICADA | Consultar producto |
| PRO-03 | `POST` | `/api/productos` | ADMINISTRADOR | Crear producto |
| PRO-04 | `PUT` | `/api/productos/{id}` | ADMINISTRADOR | Actualizar producto |
| PRO-05 | `DELETE` | `/api/productos/{id}` | ADMINISTRADOR | Dar de baja producto |
| MOV-01 | `POST` | `/api/movimientos` | AUTENTICADA | Registrar movimiento |
| MOV-02 | `GET` | `/api/movimientos` | AUTENTICADA | Consultar historial de movimientos |
| MOV-03 | `GET` | `/api/movimientos/{id}` | AUTENTICADA | Consultar movimiento |
| REP-01 | `GET` | `/api/alertas/stock-bajo` | AUTENTICADA | Alertas de stock bajo |
| REP-02 | `GET` | `/api/reportes/resumen` | AUTENTICADA | Resumen del inventario |

## 3. Detalle de cada servicio

### Sistema

#### SIS-01 · Índice de rutas

`GET /api` — **Acceso:** Pública (sin token)

Lista todas las rutas registradas con su método y nivel de acceso.

**Respuestas posibles:** `200` Lista de rutas.

<details><summary>Ejemplo exitoso (200)</summary>

Petición:
```http
GET /api
```
Respuesta `200`:
```json
{
  "datos": [
    {
      "metodo": "GET",
      "ruta": "/api",
      "acceso": "PUBLICA"
    },
    {
      "metodo": "GET",
      "ruta": "/api/salud",
      "acceso": "PUBLICA"
    },
    "..."
  ],
  "total": 28
}
```
</details>

#### SIS-02 · Estado del servicio

`GET /api/salud` — **Acceso:** Pública (sin token)

Permite a herramientas de monitoreo comprobar que la API responde. Indica el modo de persistencia activo (mysql o memoria).

**Respuestas posibles:** `200` Servicio en línea.

<details><summary>Ejemplo exitoso (200)</summary>

Petición:
```http
GET /api/salud
```
Respuesta `200`:
```json
{
  "datos": {
    "estado": "OK",
    "servicio": "API de inventario Raspicholados",
    "version": "1.0.0",
    "persistencia": "memoria",
    "segundosEnLinea": 2,
    "fecha": "2026-11-14T08:30:00"
  }
}
```
</details>

#### SIS-03 · Especificación OpenAPI

`GET /api/docs` — **Acceso:** Pública (sin token)

Devuelve este contrato en formato OpenAPI 3.0.3 (JSON). Se puede importar en Postman o en editor.swagger.io.

**Respuestas posibles:** `200` Documento OpenAPI.

<details><summary>Ejemplo exitoso (200)</summary>

Petición:
```http
GET /api/docs
```
Respuesta `200`:
```json
{
  "openapi": "3.0.3",
  "info": {
    "title": "API de inventario Raspicholados",
    "version": "1.0.0",
    "...": "..."
  },
  "paths": {
    "...": "..."
  }
}
```
</details>

### Autenticación y usuarios

#### AUT-01 · Iniciar sesión

`POST /api/auth/login` — **Acceso:** Pública (sin token)

Valida usuario y contraseña. Si son correctos entrega un token JWT (HS256) que debe enviarse en el encabezado Authorization: Bearer <token> de las demás peticiones.

**Reglas de negocio y validaciones:**

- El mensaje de error es el mismo si el usuario no existe o si la clave es incorrecta.
- Cinco fallos seguidos bloquean la cuenta 15 minutos.

**Respuestas posibles:** `200` Autenticación exitosa: token, tipo, expiraEnSegundos y usuario; `400` JSON malformado o campos faltantes; `401` Usuario o contraseña incorrectos; `415` Content-Type distinto de application/json; `429` Cuenta bloqueada por 15 minutos tras 5 intentos fallidos (incluye Retry-After).

<details><summary>Ejemplo exitoso (200)</summary>

Petición:
```http
POST /api/auth/login
Content-Type: application/json

{
  "usuario": "admin",
  "contrasena": "Raspi2026*"
}
```
Respuesta `200`:
```json
{
  "mensaje": "Autenticación exitosa",
  "datos": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...(token JWT)",
    "tipo": "Bearer",
    "expiraEnSegundos": 7200,
    "usuario": {
      "id": 1,
      "nombreUsuario": "admin",
      "nombreCompleto": "Administrador Raspicholados",
      "rol": "ADMINISTRADOR",
      "activo": true,
      "fechaRegistro": "2026-11-14T08:30:00"
    }
  }
}
```
</details>

<details><summary>Ejemplo de error (401)</summary>

Petición:
```http
POST /api/auth/login
Content-Type: application/json

{
  "usuario": "admin",
  "contrasena": "Incorrecta1"
}
```
Respuesta `401`:
```json
{
  "error": {
    "estado": 401,
    "codigo": "CREDENCIALES_INVALIDAS",
    "mensaje": "Usuario o contraseña incorrectos. Intentos restantes antes del bloqueo: 4",
    "ruta": "/api/auth/login",
    "fecha": "2026-11-14T08:30:00"
  }
}
```
</details>

#### AUT-02 · Perfil del usuario autenticado

`GET /api/auth/perfil` — **Acceso:** Token de cualquier rol

Devuelve los datos del usuario dueño del token (sin el hash de la contraseña).

**Respuestas posibles:** `200` Datos del usuario; `401` Token ausente, inválido o expirado.

<details><summary>Ejemplo exitoso (200)</summary>

Petición:
```http
GET /api/auth/perfil
Authorization: Bearer <token>
```
Respuesta `200`:
```json
{
  "datos": {
    "id": 2,
    "nombreUsuario": "laura.gomez",
    "nombreCompleto": "Laura Gómez",
    "rol": "EMPLEADO",
    "activo": true,
    "fechaRegistro": "2026-11-14T08:30:00"
  }
}
```
</details>

<details><summary>Ejemplo de error (401)</summary>

Petición:
```http
GET /api/auth/perfil
```
Respuesta `401`:
```json
{
  "error": {
    "estado": 401,
    "codigo": "TOKEN_REQUERIDO",
    "mensaje": "Debe enviar el encabezado Authorization: Bearer <token>. Obtenga el token en POST /api/auth/login",
    "ruta": "/api/auth/perfil",
    "fecha": "2026-11-14T08:30:00"
  }
}
```
</details>

#### AUT-03 · Cambiar la propia contraseña

`PUT /api/auth/contrasena` — **Acceso:** Token de cualquier rol

El usuario autenticado cambia su contraseña indicando la actual y la nueva.

**Reglas de negocio y validaciones:**

- La nueva contraseña debe tener 8 a 64 caracteres, al menos una letra y un número.

**Respuestas posibles:** `200` Contraseña actualizada; `400` La nueva no cumple la política o es igual a la actual; `401` La contraseña actual no es correcta.

<details><summary>Ejemplo exitoso (200)</summary>

Petición:
```http
PUT /api/auth/contrasena
Authorization: Bearer <token>
Content-Type: application/json

{
  "contrasenaActual": "Empleado2026",
  "contrasenaNueva": "NuevaClave2026"
}
```
Respuesta `200`:
```json
{
  "mensaje": "Contraseña actualizada correctamente"
}
```
</details>

<details><summary>Ejemplo de error (401)</summary>

Petición:
```http
PUT /api/auth/contrasena
Authorization: Bearer <token>
Content-Type: application/json

{
  "contrasenaActual": "Mala1234",
  "contrasenaNueva": "OtraClave2026"
}
```
Respuesta `401`:
```json
{
  "error": {
    "estado": 401,
    "codigo": "CREDENCIALES_INVALIDAS",
    "mensaje": "La contraseña actual no es correcta",
    "ruta": "/api/auth/contrasena",
    "fecha": "2026-11-14T08:30:00"
  }
}
```
</details>

#### USU-01 · Registrar usuario

`POST /api/usuarios` — **Acceso:** Token con rol ADMINISTRADOR

El administrador crea cuentas para los empleados de la heladería (o para otro administrador).

**Reglas de negocio y validaciones:**

- nombreUsuario: 3 a 30 caracteres (letras, números, punto, guion, guion bajo), único.
- rol: ADMINISTRADOR o EMPLEADO (por defecto EMPLEADO).
- La contraseña se guarda con PBKDF2-HMAC-SHA256 (210 000 iteraciones y salt aleatoria).

**Respuestas posibles:** `201` Usuario creado (cabecera Location); `400` Datos inválidos; `401` Token ausente, inválido o expirado; `403` El rol no tiene permiso; `409` El nombre de usuario ya existe.

<details><summary>Ejemplo exitoso (201)</summary>

Petición:
```http
POST /api/usuarios
Authorization: Bearer <token>
Content-Type: application/json

{
  "nombreUsuario": "laura.gomez",
  "nombreCompleto": "Laura Gómez",
  "contrasena": "Empleado2026",
  "rol": "EMPLEADO"
}
```
Respuesta `201`:
```json
{
  "mensaje": "Usuario registrado correctamente",
  "datos": {
    "id": 2,
    "nombreUsuario": "laura.gomez",
    "nombreCompleto": "Laura Gómez",
    "rol": "EMPLEADO",
    "activo": true,
    "fechaRegistro": "2026-11-14T08:30:00"
  }
}
```
</details>

<details><summary>Ejemplo de error (400)</summary>

Petición:
```http
POST /api/usuarios
Authorization: Bearer <token>
Content-Type: application/json

{
  "nombreUsuario": "x",
  "nombreCompleto": "Prueba",
  "contrasena": "abc"
}
```
Respuesta `400`:
```json
{
  "error": {
    "estado": 400,
    "codigo": "VALIDACION_FALLIDA",
    "mensaje": "Los datos enviados no son válidos (3 error(es))",
    "detalles": [
      {
        "campo": "nombreUsuario",
        "mensaje": "Use de 3 a 30 caracteres: letras, números, punto, guion o guion bajo"
      },
      {
        "campo": "contrasena",
        "mensaje": "Debe tener al menos 8 caracteres"
      },
      "..."
    ],
    "ruta": "/api/usuarios",
    "fecha": "2026-11-14T08:30:00"
  }
}
```
</details>

#### USU-02 · Listar usuarios

`GET /api/usuarios` — **Acceso:** Token con rol ADMINISTRADOR

Lista los usuarios registrados.

**Respuestas posibles:** `200` Lista de usuarios; `401` Token ausente, inválido o expirado; `403` El rol no tiene permiso.

<details><summary>Ejemplo exitoso (200)</summary>

Petición:
```http
GET /api/usuarios
Authorization: Bearer <token>
```
Respuesta `200`:
```json
{
  "datos": [
    {
      "id": 1,
      "nombreUsuario": "admin",
      "nombreCompleto": "Administrador Raspicholados",
      "rol": "ADMINISTRADOR",
      "activo": true,
      "fechaRegistro": "2026-11-14T08:30:00"
    },
    {
      "id": 2,
      "nombreUsuario": "laura.gomez",
      "nombreCompleto": "Laura Gómez",
      "rol": "EMPLEADO",
      "activo": true,
      "fechaRegistro": "2026-11-14T08:30:00"
    }
  ],
  "total": 2
}
```
</details>

<details><summary>Ejemplo de error (403)</summary>

Petición:
```http
GET /api/usuarios
Authorization: Bearer <token>
```
Respuesta `403`:
```json
{
  "error": {
    "estado": 403,
    "codigo": "PERMISO_DENEGADO",
    "mensaje": "Esta operación solo la puede realizar un usuario con rol ADMINISTRADOR",
    "ruta": "/api/usuarios",
    "fecha": "2026-11-14T08:30:00"
  }
}
```
</details>

### Categorías

#### CAT-01 · Listar categorías

`GET /api/categorias` — **Acceso:** Token de cualquier rol

Lista las categorías con la cantidad de productos activos de cada una.

**Respuestas posibles:** `200` Lista; `401` Token ausente, inválido o expirado.

<details><summary>Ejemplo exitoso (200)</summary>

Petición:
```http
GET /api/categorias
Authorization: Bearer <token>
```
Respuesta `200`:
```json
{
  "datos": [
    {
      "id": 1,
      "nombre": "Raspados",
      "descripcion": "Raspados de hielo con jarabe de fruta",
      "cantidadProductos": 2
    },
    {
      "id": 2,
      "nombre": "Helados",
      "descripcion": "Helados por litro para servir en copa o cono",
      "cantidadProductos": 2
    },
    "..."
  ],
  "total": 5
}
```
</details>

#### CAT-02 · Consultar categoría

`GET /api/categorias/{id}` — **Acceso:** Token de cualquier rol

Obtiene una categoría por su id.

| Parámetro | Ubicación | Tipo | Obligatorio | Descripción |
|---|---|---|---|---|
| `id` | ruta | integer | Sí | Identificador del recurso (entero positivo) |

**Respuestas posibles:** `200` Categoría; `400` Id no numérico; `401` Token ausente, inválido o expirado; `404` No existe.

<details><summary>Ejemplo exitoso (200)</summary>

Petición:
```http
GET /api/categorias/6
Authorization: Bearer <token>
```
Respuesta `200`:
```json
{
  "datos": {
    "id": 6,
    "nombre": "Granizados",
    "descripcion": "Granizados de fruta natural",
    "cantidadProductos": 0
  }
}
```
</details>

<details><summary>Ejemplo de error (404)</summary>

Petición:
```http
GET /api/categorias/99
Authorization: Bearer <token>
```
Respuesta `404`:
```json
{
  "error": {
    "estado": 404,
    "codigo": "RECURSO_NO_ENCONTRADO",
    "mensaje": "No existe una categoría con id 99",
    "ruta": "/api/categorias/99",
    "fecha": "2026-11-14T08:30:00"
  }
}
```
</details>

#### CAT-03 · Crear categoría

`POST /api/categorias` — **Acceso:** Token con rol ADMINISTRADOR

Registra una nueva categoría de productos.

**Reglas de negocio y validaciones:**

- nombre: 2 a 50 caracteres, único sin distinguir mayúsculas.

**Respuestas posibles:** `201` Creada; `400` Datos inválidos; `401` Token ausente, inválido o expirado; `403` El rol no tiene permiso; `409` Ya existe una categoría con ese nombre.

<details><summary>Ejemplo exitoso (201)</summary>

Petición:
```http
POST /api/categorias
Authorization: Bearer <token>
Content-Type: application/json

{
  "nombre": "Granizados",
  "descripcion": "Granizados de fruta natural"
}
```
Respuesta `201`:
```json
{
  "mensaje": "Categoría creada correctamente",
  "datos": {
    "id": 6,
    "nombre": "Granizados",
    "descripcion": "Granizados de fruta natural",
    "cantidadProductos": 0
  }
}
```
</details>

<details><summary>Ejemplo de error (409)</summary>

Petición:
```http
POST /api/categorias
Authorization: Bearer <token>
Content-Type: application/json

{
  "nombre": "granizados"
}
```
Respuesta `409`:
```json
{
  "error": {
    "estado": 409,
    "codigo": "CATEGORIA_DUPLICADA",
    "mensaje": "Ya existe una categoría llamada 'granizados'",
    "ruta": "/api/categorias",
    "fecha": "2026-11-14T08:30:00"
  }
}
```
</details>

#### CAT-04 · Actualizar categoría

`PUT /api/categorias/{id}` — **Acceso:** Token con rol ADMINISTRADOR

Reemplaza nombre y descripción. Si cambia el nombre, los productos de la categoría se actualizan en cascada.

| Parámetro | Ubicación | Tipo | Obligatorio | Descripción |
|---|---|---|---|---|
| `id` | ruta | integer | Sí | Identificador del recurso (entero positivo) |

**Respuestas posibles:** `200` Actualizada; `400` Datos inválidos; `401` Token ausente, inválido o expirado; `403` El rol no tiene permiso; `404` No existe; `409` Nombre repetido.

<details><summary>Ejemplo exitoso (200)</summary>

Petición:
```http
PUT /api/categorias/6
Authorization: Bearer <token>
Content-Type: application/json

{
  "nombre": "Granizados Premium",
  "descripcion": "Línea premium"
}
```
Respuesta `200`:
```json
{
  "mensaje": "Categoría actualizada correctamente",
  "datos": {
    "id": 6,
    "nombre": "Granizados Premium",
    "descripcion": "Línea premium",
    "cantidadProductos": 0
  }
}
```
</details>

<details><summary>Ejemplo de error (409)</summary>

Petición:
```http
PUT /api/categorias/6
Authorization: Bearer <token>
Content-Type: application/json

{
  "nombre": "Helados"
}
```
Respuesta `409`:
```json
{
  "error": {
    "estado": 409,
    "codigo": "CATEGORIA_DUPLICADA",
    "mensaje": "Ya existe una categoría llamada 'Helados'",
    "ruta": "/api/categorias/6",
    "fecha": "2026-11-14T08:30:00"
  }
}
```
</details>

#### CAT-05 · Eliminar categoría

`DELETE /api/categorias/{id}` — **Acceso:** Token con rol ADMINISTRADOR

Elimina una categoría que no tenga productos asociados.

| Parámetro | Ubicación | Tipo | Obligatorio | Descripción |
|---|---|---|---|---|
| `id` | ruta | integer | Sí | Identificador del recurso (entero positivo) |

**Respuestas posibles:** `200` Eliminada; `401` Token ausente, inválido o expirado; `403` El rol no tiene permiso; `404` No existe; `409` Tiene productos asociados (incluidos inactivos).

<details><summary>Ejemplo exitoso (200)</summary>

Petición:
```http
DELETE /api/categorias/7
Authorization: Bearer <token>
```
Respuesta `200`:
```json
{
  "mensaje": "Categoría eliminada correctamente"
}
```
</details>

<details><summary>Ejemplo de error (409)</summary>

Petición:
```http
DELETE /api/categorias/1
Authorization: Bearer <token>
```
Respuesta `409`:
```json
{
  "error": {
    "estado": 409,
    "codigo": "CATEGORIA_CON_PRODUCTOS",
    "mensaje": "No se puede eliminar la categoría 'Raspados' porque tiene 2 producto(s) asociado(s) (incluye productos inactivos). Reasígnelos primero.",
    "ruta": "/api/categorias/1",
    "fecha": "2026-11-14T08:30:00"
  }
}
```
</details>

### Proveedores

#### PRV-01 · Listar proveedores

`GET /api/proveedores` — **Acceso:** Token de cualquier rol

Lista los proveedores ordenados por nombre.

**Respuestas posibles:** `200` Lista; `401` Token ausente, inválido o expirado.

<details><summary>Ejemplo exitoso (200)</summary>

Petición:
```http
GET /api/proveedores
Authorization: Bearer <token>
```
Respuesta `200`:
```json
{
  "datos": [
    {
      "id": 1,
      "nit": "900123456-1",
      "nombre": "Frutas del Valle S.A.S.",
      "telefono": "3104567890",
      "correo": "ventas@frutasdelvalle.co",
      "direccion": "Cra 5 # 12-30, Cali"
    },
    {
      "id": 2,
      "nit": "800765432-9",
      "nombre": "Lácteos San Rafael",
      "telefono": "3157654321",
      "correo": "pedidos@lacteossanrafael.co",
      "direccion": "Cl 25 # 8-14, Palmira"
    },
    "..."
  ],
  "total": 4
}
```
</details>

#### PRV-02 · Consultar proveedor

`GET /api/proveedores/{id}` — **Acceso:** Token de cualquier rol

Obtiene un proveedor por su id.

| Parámetro | Ubicación | Tipo | Obligatorio | Descripción |
|---|---|---|---|---|
| `id` | ruta | integer | Sí | Identificador del recurso (entero positivo) |

**Respuestas posibles:** `200` Proveedor; `400` Id no numérico; `401` Token ausente, inválido o expirado; `404` No existe.

<details><summary>Ejemplo exitoso (200)</summary>

Petición:
```http
GET /api/proveedores/5
Authorization: Bearer <token>
```
Respuesta `200`:
```json
{
  "datos": {
    "id": 5,
    "nit": "901234567-8",
    "nombre": "Pulpas del Pacífico",
    "telefono": "3001234567",
    "correo": "ventas@pulpaspacifico.co",
    "direccion": "Cl 10 # 4-20, Buenaventura"
  }
}
```
</details>

<details><summary>Ejemplo de error (400)</summary>

Petición:
```http
GET /api/proveedores/abc
Authorization: Bearer <token>
```
Respuesta `400`:
```json
{
  "error": {
    "estado": 400,
    "codigo": "PARAMETRO_INVALIDO",
    "mensaje": "El parámetro de ruta 'id' debe ser un número entero positivo; se recibió 'abc'",
    "ruta": "/api/proveedores/abc",
    "fecha": "2026-11-14T08:30:00"
  }
}
```
</details>

#### PRV-03 · Crear proveedor

`POST /api/proveedores` — **Acceso:** Token con rol ADMINISTRADOR

Registra un proveedor de insumos.

**Reglas de negocio y validaciones:**

- nit: 6 a 12 dígitos con dígito de verificación opcional (900123456-1), único.
- telefono (opcional): 7 a 20 caracteres numéricos.
- correo (opcional): formato de correo válido.

**Respuestas posibles:** `201` Creado; `400` Datos inválidos; `401` Token ausente, inválido o expirado; `403` El rol no tiene permiso; `409` NIT repetido.

<details><summary>Ejemplo exitoso (201)</summary>

Petición:
```http
POST /api/proveedores
Authorization: Bearer <token>
Content-Type: application/json

{
  "nit": "901234567-8",
  "nombre": "Pulpas del Pacífico",
  "telefono": "3001234567",
  "correo": "ventas@pulpaspacifico.co",
  "direccion": "Cl 10 # 4-20, Buenaventura"
}
```
Respuesta `201`:
```json
{
  "mensaje": "Proveedor creado correctamente",
  "datos": {
    "id": 5,
    "nit": "901234567-8",
    "nombre": "Pulpas del Pacífico",
    "telefono": "3001234567",
    "correo": "ventas@pulpaspacifico.co",
    "direccion": "Cl 10 # 4-20, Buenaventura"
  }
}
```
</details>

<details><summary>Ejemplo de error (400)</summary>

Petición:
```http
POST /api/proveedores
Authorization: Bearer <token>
Content-Type: application/json

{
  "nit": "ABC",
  "nombre": "Xy",
  "correo": "no-es-correo"
}
```
Respuesta `400`:
```json
{
  "error": {
    "estado": 400,
    "codigo": "VALIDACION_FALLIDA",
    "mensaje": "Los datos enviados no son válidos (3 error(es))",
    "detalles": [
      {
        "campo": "nit",
        "mensaje": "Formato de NIT inválido. Ejemplo: 900123456-1"
      },
      {
        "campo": "nombre",
        "mensaje": "Debe tener al menos 3 caracteres"
      },
      "..."
    ],
    "ruta": "/api/proveedores",
    "fecha": "2026-11-14T08:30:00"
  }
}
```
</details>

#### PRV-04 · Actualizar proveedor

`PUT /api/proveedores/{id}` — **Acceso:** Token con rol ADMINISTRADOR

Reemplaza los datos del proveedor.

| Parámetro | Ubicación | Tipo | Obligatorio | Descripción |
|---|---|---|---|---|
| `id` | ruta | integer | Sí | Identificador del recurso (entero positivo) |

**Respuestas posibles:** `200` Actualizado; `400` Datos inválidos; `401` Token ausente, inválido o expirado; `403` El rol no tiene permiso; `404` No existe; `409` NIT repetido.

<details><summary>Ejemplo exitoso (200)</summary>

Petición:
```http
PUT /api/proveedores/5
Authorization: Bearer <token>
Content-Type: application/json

{
  "nit": "901234567-8",
  "nombre": "Pulpas del Pacífico S.A.S.",
  "telefono": "3001234567",
  "correo": "ventas@pulpaspacifico.co"
}
```
Respuesta `200`:
```json
{
  "mensaje": "Proveedor actualizado correctamente",
  "datos": {
    "id": 5,
    "nit": "901234567-8",
    "nombre": "Pulpas del Pacífico S.A.S.",
    "telefono": "3001234567",
    "correo": "ventas@pulpaspacifico.co",
    "direccion": null
  }
}
```
</details>

<details><summary>Ejemplo de error (409)</summary>

Petición:
```http
PUT /api/proveedores/5
Authorization: Bearer <token>
Content-Type: application/json

{
  "nit": "900123456-1",
  "nombre": "Repetido"
}
```
Respuesta `409`:
```json
{
  "error": {
    "estado": 409,
    "codigo": "PROVEEDOR_DUPLICADO",
    "mensaje": "Ya existe un proveedor con el NIT 900123456-1",
    "ruta": "/api/proveedores/5",
    "fecha": "2026-11-14T08:30:00"
  }
}
```
</details>

#### PRV-05 · Eliminar proveedor

`DELETE /api/proveedores/{id}` — **Acceso:** Token con rol ADMINISTRADOR

Elimina un proveedor que no surta productos activos. En los productos inactivos el proveedor queda en null.

| Parámetro | Ubicación | Tipo | Obligatorio | Descripción |
|---|---|---|---|---|
| `id` | ruta | integer | Sí | Identificador del recurso (entero positivo) |

**Respuestas posibles:** `200` Eliminado; `401` Token ausente, inválido o expirado; `403` El rol no tiene permiso; `404` No existe; `409` Surte productos activos.

<details><summary>Ejemplo exitoso (200)</summary>

Petición:
```http
DELETE /api/proveedores/5
Authorization: Bearer <token>
```
Respuesta `200`:
```json
{
  "mensaje": "Proveedor eliminado correctamente"
}
```
</details>

<details><summary>Ejemplo de error (409)</summary>

Petición:
```http
DELETE /api/proveedores/5
Authorization: Bearer <token>
```
Respuesta `409`:
```json
{
  "error": {
    "estado": 409,
    "codigo": "PROVEEDOR_CON_PRODUCTOS",
    "mensaje": "No se puede eliminar a 'Pulpas del Pacífico S.A.S.' porque surte 1 producto(s) activo(s). Asigne otro proveedor a esos productos primero.",
    "ruta": "/api/proveedores/5",
    "fecha": "2026-11-14T08:30:00"
  }
}
```
</details>

### Productos

#### PRO-01 · Listar y filtrar productos

`GET /api/productos` — **Acceso:** Token de cualquier rol

Lista los productos activos. Todos los filtros son opcionales y combinables.

| Parámetro | Ubicación | Tipo | Obligatorio | Descripción |
|---|---|---|---|---|
| `categoria` | consulta | string | No | Nombre exacto de la categoría |
| `buscar` | consulta | string | No | Texto contenido en el nombre |
| `proveedorId` | consulta | integer | No | Id del proveedor |
| `stockBajo` | consulta | boolean | No | true = solo productos BAJO o AGOTADO |

**Respuestas posibles:** `200` Lista de productos; `400` Parámetro inválido; `401` Token ausente, inválido o expirado.

<details><summary>Ejemplo exitoso (200)</summary>

Petición:
```http
GET /api/productos?categoria=Helados
Authorization: Bearer <token>
```
Respuesta `200`:
```json
{
  "datos": [
    {
      "id": 6,
      "nombre": "Helado de vainilla",
      "categoria": "Helados",
      "unidadMedida": "Litro",
      "stockActual": 12,
      "stockMinimo": 5,
      "estadoStock": "NORMAL",
      "precioUnitario": 18000,
      "valorInventario": 216000,
      "proveedorId": 2,
      "proveedorNombre": "Lácteos San Rafael",
      "activo": true,
      "fechaRegistro": "2026-11-14T08:30:00"
    },
    {
      "id": 7,
      "nombre": "Helado de chocolate",
      "categoria": "Helados",
      "unidadMedida": "Litro",
      "stockActual": 0,
      "stockMinimo": 5,
      "estadoStock": "AGOTADO",
      "precioUnitario": 18500,
      "valorInventario": 0,
      "proveedorId": 2,
      "proveedorNombre": "Lácteos San Rafael",
      "activo": true,
      "fechaRegistro": "2026-11-14T08:30:00"
    }
  ],
  "total": 2
}
```
</details>

<details><summary>Ejemplo de error (401)</summary>

Petición:
```http
GET /api/productos
```
Respuesta `401`:
```json
{
  "error": {
    "estado": 401,
    "codigo": "TOKEN_REQUERIDO",
    "mensaje": "Debe enviar el encabezado Authorization: Bearer <token>. Obtenga el token en POST /api/auth/login",
    "ruta": "/api/productos",
    "fecha": "2026-11-14T08:30:00"
  }
}
```
</details>

#### PRO-02 · Consultar producto

`GET /api/productos/{id}` — **Acceso:** Token de cualquier rol

Obtiene un producto activo con su estado de stock y valor de inventario.

| Parámetro | Ubicación | Tipo | Obligatorio | Descripción |
|---|---|---|---|---|
| `id` | ruta | integer | Sí | Identificador del recurso (entero positivo) |

**Respuestas posibles:** `200` Producto; `400` Id no numérico; `401` Token ausente, inválido o expirado; `404` No existe o fue dado de baja.

<details><summary>Ejemplo exitoso (200)</summary>

Petición:
```http
GET /api/productos/10
Authorization: Bearer <token>
```
Respuesta `200`:
```json
{
  "datos": {
    "id": 10,
    "nombre": "Pulpa de maracuyá",
    "categoria": "Granizados Premium",
    "unidadMedida": "Kilogramo",
    "stockActual": 25,
    "stockMinimo": 8,
    "estadoStock": "NORMAL",
    "precioUnitario": 12500.0,
    "valorInventario": 312500.0,
    "proveedorId": 5,
    "proveedorNombre": "Pulpas del Pacífico S.A.S.",
    "activo": true,
    "fechaRegistro": "2026-11-14T08:30:00"
  }
}
```
</details>

<details><summary>Ejemplo de error (404)</summary>

Petición:
```http
GET /api/productos/999
Authorization: Bearer <token>
```
Respuesta `404`:
```json
{
  "error": {
    "estado": 404,
    "codigo": "RECURSO_NO_ENCONTRADO",
    "mensaje": "No existe un producto activo con id 999",
    "ruta": "/api/productos/999",
    "fecha": "2026-11-14T08:30:00"
  }
}
```
</details>

#### PRO-03 · Crear producto

`POST /api/productos` — **Acceso:** Token con rol ADMINISTRADOR

Registra un producto. Si se envía stockInicial mayor que 0, se registra automáticamente un movimiento de ENTRADA 'Inventario inicial'.

**Reglas de negocio y validaciones:**

- La categoría y el proveedor deben existir.
- precioUnitario >= 0 con máximo 2 decimales; stockMinimo entero >= 0.
- No se acepta stockActual: el stock solo cambia con movimientos.

**Respuestas posibles:** `201` Creado; `400` Datos inválidos (se listan todos los errores); `401` Token ausente, inválido o expirado; `403` El rol no tiene permiso; `409` Ya existe un producto activo con ese nombre.

<details><summary>Ejemplo exitoso (201)</summary>

Petición:
```http
POST /api/productos
Authorization: Bearer <token>
Content-Type: application/json

{
  "nombre": "Pulpa de maracuyá",
  "categoria": "Granizados Premium",
  "unidadMedida": "Kilogramo",
  "stockMinimo": 8,
  "precioUnitario": 12500,
  "proveedorId": 5,
  "stockInicial": 25
}
```
Respuesta `201`:
```json
{
  "mensaje": "Producto creado correctamente",
  "datos": {
    "id": 10,
    "nombre": "Pulpa de maracuyá",
    "categoria": "Granizados Premium",
    "unidadMedida": "Kilogramo",
    "stockActual": 25,
    "stockMinimo": 8,
    "estadoStock": "NORMAL",
    "precioUnitario": 12500.0,
    "valorInventario": 312500.0,
    "proveedorId": 5,
    "proveedorNombre": "Pulpas del Pacífico S.A.S.",
    "activo": true,
    "fechaRegistro": "2026-11-14T08:30:00"
  }
}
```
</details>

<details><summary>Ejemplo de error (400)</summary>

Petición:
```http
POST /api/productos
Authorization: Bearer <token>
Content-Type: application/json

{
  "nombre": "Producto malo",
  "categoria": "Inexistente",
  "unidadMedida": "Unidad",
  "stockMinimo": 2.5,
  "precioUnitario": -5
}
```
Respuesta `400`:
```json
{
  "error": {
    "estado": 400,
    "codigo": "VALIDACION_FALLIDA",
    "mensaje": "Los datos enviados no son válidos (3 error(es))",
    "detalles": [
      {
        "campo": "stockMinimo",
        "mensaje": "Debe ser un número entero, sin decimales"
      },
      {
        "campo": "precioUnitario",
        "mensaje": "Debe ser mayor o igual a 0"
      },
      "..."
    ],
    "ruta": "/api/productos",
    "fecha": "2026-11-14T08:30:00"
  }
}
```
</details>

#### PRO-04 · Actualizar producto

`PUT /api/productos/{id}` — **Acceso:** Token con rol ADMINISTRADOR

Reemplaza los datos descriptivos del producto. El stock no se modifica aquí.

| Parámetro | Ubicación | Tipo | Obligatorio | Descripción |
|---|---|---|---|---|
| `id` | ruta | integer | Sí | Identificador del recurso (entero positivo) |

**Respuestas posibles:** `200` Actualizado; `400` Datos inválidos o se intentó cambiar el stock; `401` Token ausente, inválido o expirado; `403` El rol no tiene permiso; `404` No existe; `409` Nombre repetido.

<details><summary>Ejemplo exitoso (200)</summary>

Petición:
```http
PUT /api/productos/10
Authorization: Bearer <token>
Content-Type: application/json

{
  "nombre": "Pulpa de maracuyá",
  "categoria": "Granizados Premium",
  "unidadMedida": "Kilogramo",
  "stockMinimo": 8,
  "precioUnitario": 13000,
  "proveedorId": 5
}
```
Respuesta `200`:
```json
{
  "mensaje": "Producto actualizado correctamente",
  "datos": {
    "id": 10,
    "nombre": "Pulpa de maracuyá",
    "categoria": "Granizados Premium",
    "unidadMedida": "Kilogramo",
    "stockActual": 25,
    "stockMinimo": 8,
    "estadoStock": "NORMAL",
    "precioUnitario": 13000.0,
    "valorInventario": 325000.0,
    "proveedorId": 5,
    "proveedorNombre": "Pulpas del Pacífico S.A.S.",
    "activo": true,
    "fechaRegistro": "2026-11-14T08:30:00"
  }
}
```
</details>

<details><summary>Ejemplo de error (400)</summary>

Petición:
```http
PUT /api/productos/10
Authorization: Bearer <token>
Content-Type: application/json

{
  "nombre": "Pulpa de maracuyá",
  "categoria": "Granizados Premium",
  "unidadMedida": "Kilogramo",
  "stockMinimo": 8,
  "precioUnitario": 13000,
  "stockActual": 999
}
```
Respuesta `400`:
```json
{
  "error": {
    "estado": 400,
    "codigo": "VALIDACION_FALLIDA",
    "mensaje": "Los datos enviados no son válidos (1 error(es))",
    "detalles": [
      {
        "campo": "stockActual",
        "mensaje": "El stock no se modifica aquí. Registre un movimiento en POST /api/movimientos"
      }
    ],
    "ruta": "/api/productos/10",
    "fecha": "2026-11-14T08:30:00"
  }
}
```
</details>

#### PRO-05 · Dar de baja producto

`DELETE /api/productos/{id}` — **Acceso:** Token con rol ADMINISTRADOR

Baja lógica (activo = false). El producto deja de listarse, pero su historial de movimientos se conserva.

| Parámetro | Ubicación | Tipo | Obligatorio | Descripción |
|---|---|---|---|---|
| `id` | ruta | integer | Sí | Identificador del recurso (entero positivo) |

**Respuestas posibles:** `200` Dado de baja; `401` Token ausente, inválido o expirado; `403` El rol no tiene permiso; `404` No existe.

<details><summary>Ejemplo exitoso (200)</summary>

Petición:
```http
DELETE /api/productos/10
Authorization: Bearer <token>
```
Respuesta `200`:
```json
{
  "mensaje": "Producto dado de baja correctamente (su historial se conserva)"
}
```
</details>

### Movimientos de inventario

#### MOV-01 · Registrar movimiento

`POST /api/movimientos` — **Acceso:** Token de cualquier rol

Registra una ENTRADA (compra), SALIDA (venta, consumo o merma) o AJUSTE (conteo físico). La operación es atómica: se bloquea la fila del producto, se valida y se actualiza el stock en una sola transacción. Devuelve el movimiento, el producto actualizado y una alerta si el stock quedó BAJO o AGOTADO.

**Reglas de negocio y validaciones:**

- ENTRADA y SALIDA: cantidad entera >= 1.
- AJUSTE: cantidad >= 0 es el conteo físico y el motivo es obligatorio.
- Una SALIDA nunca puede dejar el stock negativo.
- Lo pueden registrar el administrador y los empleados; queda el usuario que lo hizo.

**Respuestas posibles:** `201` Movimiento registrado; `400` Datos inválidos; `401` Token ausente, inválido o expirado; `404` Producto inexistente; `409` STOCK_INSUFICIENTE, PRODUCTO_INACTIVO o STOCK_EXCEDE_MAXIMO.

<details><summary>Ejemplo exitoso (201)</summary>

Petición:
```http
POST /api/movimientos
Authorization: Bearer <token>
Content-Type: application/json

{
  "productoId": 10,
  "tipo": "SALIDA",
  "cantidad": 20,
  "motivo": "Producción de granizados del día"
}
```
Respuesta `201`:
```json
{
  "mensaje": "Movimiento de SALIDA registrado correctamente",
  "datos": {
    "movimiento": {
      "id": 2,
      "productoId": 10,
      "productoNombre": "Pulpa de maracuyá",
      "tipo": "SALIDA",
      "cantidad": 20,
      "stockAnterior": 25,
      "stockResultante": 5,
      "diferencia": -20,
      "motivo": "Producción de granizados del día",
      "usuarioId": 2,
      "usuarioNombre": "laura.gomez",
      "fecha": "2026-11-14T08:30:00"
    },
    "producto": {
      "id": 10,
      "nombre": "Pulpa de maracuyá",
      "categoria": "Granizados Premium",
      "unidadMedida": "Kilogramo",
      "stockActual": 5,
      "stockMinimo": 8,
      "estadoStock": "BAJO",
      "precioUnitario": 13000.0,
      "valorInventario": 65000.0,
      "proveedorId": 5,
      "proveedorNombre": "Pulpas del Pacífico S.A.S.",
      "activo": true,
      "fechaRegistro": "2026-11-14T08:30:00"
    },
    "alerta": "El producto 'Pulpa de maracuyá' quedó en estado BAJO (stock 5, mínimo 8)"
  }
}
```
</details>

<details><summary>Ejemplo de error (409)</summary>

Petición:
```http
POST /api/movimientos
Authorization: Bearer <token>
Content-Type: application/json

{
  "productoId": 10,
  "tipo": "SALIDA",
  "cantidad": 100
}
```
Respuesta `409`:
```json
{
  "error": {
    "estado": 409,
    "codigo": "STOCK_INSUFICIENTE",
    "mensaje": "No hay stock suficiente de 'Pulpa de maracuyá'",
    "detalles": [
      {
        "campo": "cantidad",
        "mensaje": "Solicitado: 100, disponible: 5"
      }
    ],
    "ruta": "/api/movimientos",
    "fecha": "2026-11-14T08:30:00"
  }
}
```
</details>

#### MOV-02 · Consultar historial de movimientos

`GET /api/movimientos` — **Acceso:** Token de cualquier rol

Historial del más reciente al más antiguo, con filtros opcionales.

| Parámetro | Ubicación | Tipo | Obligatorio | Descripción |
|---|---|---|---|---|
| `productoId` | consulta | integer | No | Id del producto |
| `tipo` | consulta | string | No | ENTRADA, SALIDA o AJUSTE |
| `desde` | consulta | string | No | Fecha inicial AAAA-MM-DD |
| `hasta` | consulta | string | No | Fecha final AAAA-MM-DD |
| `limite` | consulta | integer | No | Máximo de registros (1 a 500, por defecto 100) |

**Respuestas posibles:** `200` Lista de movimientos; `400` Parámetro inválido; `401` Token ausente, inválido o expirado.

<details><summary>Ejemplo exitoso (200)</summary>

Petición:
```http
GET /api/movimientos?productoId=10
Authorization: Bearer <token>
```
Respuesta `200`:
```json
{
  "datos": [
    {
      "id": 2,
      "productoId": 10,
      "productoNombre": "Pulpa de maracuyá",
      "tipo": "SALIDA",
      "cantidad": 20,
      "stockAnterior": 25,
      "stockResultante": 5,
      "diferencia": -20,
      "motivo": "Producción de granizados del día",
      "usuarioId": 2,
      "usuarioNombre": "laura.gomez",
      "fecha": "2026-11-14T08:30:00"
    },
    {
      "id": 1,
      "productoId": 10,
      "productoNombre": "Pulpa de maracuyá",
      "tipo": "ENTRADA",
      "cantidad": 25,
      "stockAnterior": 0,
      "stockResultante": 25,
      "diferencia": 25,
      "motivo": "Inventario inicial",
      "usuarioId": 1,
      "usuarioNombre": "admin",
      "fecha": "2026-11-14T08:30:00"
    }
  ],
  "total": 2
}
```
</details>

<details><summary>Ejemplo de error (400)</summary>

Petición:
```http
GET /api/movimientos?desde=2026-13-01
Authorization: Bearer <token>
```
Respuesta `400`:
```json
{
  "error": {
    "estado": 400,
    "codigo": "PARAMETRO_INVALIDO",
    "mensaje": "El parámetro 'desde' debe tener formato AAAA-MM-DD",
    "ruta": "/api/movimientos",
    "fecha": "2026-11-14T08:30:00"
  }
}
```
</details>

#### MOV-03 · Consultar movimiento

`GET /api/movimientos/{id}` — **Acceso:** Token de cualquier rol

Obtiene un movimiento por su id.

| Parámetro | Ubicación | Tipo | Obligatorio | Descripción |
|---|---|---|---|---|
| `id` | ruta | integer | Sí | Identificador del recurso (entero positivo) |

**Respuestas posibles:** `200` Movimiento; `401` Token ausente, inválido o expirado; `404` No existe.

<details><summary>Ejemplo exitoso (200)</summary>

Petición:
```http
GET /api/movimientos/1
Authorization: Bearer <token>
```
Respuesta `200`:
```json
{
  "datos": {
    "id": 1,
    "productoId": 10,
    "productoNombre": "Pulpa de maracuyá",
    "tipo": "ENTRADA",
    "cantidad": 25,
    "stockAnterior": 0,
    "stockResultante": 25,
    "diferencia": 25,
    "motivo": "Inventario inicial",
    "usuarioId": 1,
    "usuarioNombre": "admin",
    "fecha": "2026-11-14T08:30:00"
  }
}
```
</details>

<details><summary>Ejemplo de error (404)</summary>

Petición:
```http
GET /api/movimientos/999
Authorization: Bearer <token>
```
Respuesta `404`:
```json
{
  "error": {
    "estado": 404,
    "codigo": "RECURSO_NO_ENCONTRADO",
    "mensaje": "No existe un movimiento con id 999",
    "ruta": "/api/movimientos/999",
    "fecha": "2026-11-14T08:30:00"
  }
}
```
</details>

### Reportes

#### REP-01 · Alertas de stock bajo

`GET /api/alertas/stock-bajo` — **Acceso:** Token de cualquier rol

Productos en estado AGOTADO o BAJO, primero los agotados, con la cantidad sugerida para pedir (hasta el doble del mínimo) y el proveedor a contactar.

**Respuestas posibles:** `200` Lista de alertas; `401` Token ausente, inválido o expirado.

<details><summary>Ejemplo exitoso (200)</summary>

Petición:
```http
GET /api/alertas/stock-bajo
Authorization: Bearer <token>
```
Respuesta `200`:
```json
{
  "datos": [
    {
      "productoId": 7,
      "nombre": "Helado de chocolate",
      "categoria": "Helados",
      "estadoStock": "AGOTADO",
      "stockActual": 0,
      "stockMinimo": 5,
      "cantidadSugeridaPedido": 10,
      "unidadMedida": "Litro",
      "proveedorId": 2,
      "proveedorNombre": "Lácteos San Rafael"
    },
    {
      "productoId": 10,
      "nombre": "Pulpa de maracuyá",
      "categoria": "Granizados Premium",
      "estadoStock": "BAJO",
      "stockActual": 5,
      "stockMinimo": 8,
      "cantidadSugeridaPedido": 11,
      "unidadMedida": "Kilogramo",
      "proveedorId": 5,
      "proveedorNombre": "Pulpas del Pacífico S.A.S."
    },
    "..."
  ],
  "total": 4
}
```
</details>

#### REP-02 · Resumen del inventario

`GET /api/reportes/resumen` — **Acceso:** Token de cualquier rol

Indicadores para el administrador: productos activos, unidades, valor total del inventario, agotados, stock bajo, productos por categoría, movimientos del día y últimos 5 movimientos.

**Respuestas posibles:** `200` Resumen; `401` Token ausente, inválido o expirado.

<details><summary>Ejemplo exitoso (200)</summary>

Petición:
```http
GET /api/reportes/resumen
Authorization: Bearer <token>
```
Respuesta `200`:
```json
{
  "datos": {
    "fechaCorte": "2026-11-14T08:30:00",
    "totalProductosActivos": 10,
    "totalUnidadesEnStock": 327,
    "valorTotalInventario": 844900.0,
    "productosAgotados": 1,
    "productosStockBajo": 3,
    "productosPorCategoria": {
      "Insumos": 1,
      "Toppings": 2,
      "Raspados": 2,
      "Helados": 2,
      "Empaques": 2,
      "Granizados Premium": 1
    },
    "movimientosHoy": 2,
    "unidadesEntradasHoy": 25,
    "unidadesSalidasHoy": 20,
    "ultimosMovimientos": [
      {
        "id": 2,
        "productoId": 10,
        "productoNombre": "Pulpa de maracuyá",
        "tipo": "SALIDA",
        "cantidad": 20,
        "stockAnterior": 25,
        "stockResultante": 5,
        "diferencia": -20,
        "motivo": "Producción de granizados del día",
        "usuarioId": 2,
        "usuarioNombre": "laura.gomez",
        "fecha": "2026-11-14T08:30:00"
      },
      {
        "id": 1,
        "productoId": 10,
        "productoNombre": "Pulpa de maracuyá",
        "tipo": "ENTRADA",
        "cantidad": 25,
        "stockAnterior": 0,
        "stockResultante": 25,
        "diferencia": 25,
        "motivo": "Inventario inicial",
        "usuarioId": 1,
        "usuarioNombre": "admin",
        "fecha": "2026-11-14T08:30:00"
      }
    ]
  }
}
```
</details>

## 4. Flujo típico de uso

1. `POST /api/auth/login` con el usuario administrador → se obtiene el `token`.
2. `POST /api/categorias` y `POST /api/proveedores` → se crean los catálogos.
3. `POST /api/productos` con `stockInicial` → el producto queda con su inventario inicial trazado.
4. Durante el día, los empleados registran `POST /api/movimientos` (SALIDA por ventas, ENTRADA por compras).
5. El administrador consulta `GET /api/alertas/stock-bajo` para hacer pedidos y `GET /api/reportes/resumen` para el cierre.

## 5. Pruebas

La colección `postman/Raspicholados_API_Inventario.postman_collection.json` contiene 67 peticiones con pruebas automáticas
(código de estado, campos y reglas de negocio). Se ejecuta con el entorno `postman/Raspicholados_Inventario_Local.postman_environment.json`
desde *Run collection*. Resultado obtenido el 14/11/2026: 67 peticiones, 205 aserciones y 0 fallos con MySQL (MariaDB 10.11),
y 67/67 casos aprobados en modo memoria. El reporte HTML está en `pruebas/reporte_newman_aa5_ev03.html`.
