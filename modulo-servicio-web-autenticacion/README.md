# Servicio Web de Autenticación — Raspicholados

**Evidencia:** GA7-220501096-AA5-EV01 — Diseño y desarrollo de servicios web
**Aprendiz:** Viviana Arias Montilla
**Programa:** Tecnología en Análisis y Desarrollo de Software (SENA)

## 1. Descripción

Servicio web (API REST) que expone dos operaciones:

| Método | Ruta            | Descripción                                  |
|--------|-----------------|-----------------------------------------------|
| POST   | `/api/registro` | Registra un nuevo usuario (usuario/contraseña) |
| POST   | `/api/login`    | Valida las credenciales de un usuario          |

El servicio recibe un usuario y una contraseña en formato JSON. Si la
autenticación es correcta, responde con un mensaje de autenticación
satisfactoria (HTTP 200); en caso contrario, responde con un error de
autenticación (HTTP 401).

## 2. Tecnología utilizada

- **Lenguaje:** Java 8 o superior (compilado y probado con objetivo Java 8 y con JDK 21)
- **Servidor HTTP:** `com.sun.net.httpserver.HttpServer` (nativo del JDK,
  sin dependencias externas — no requiere Maven, Gradle ni Tomcat)
- **Persistencia:** en memoria (`ConcurrentHashMap`) con respaldo en
  archivo de texto (`usuarios.txt`), generado automáticamente al ejecutar
- **Seguridad:** contraseñas nunca almacenadas en texto plano; se guarda
  un hash SHA-256 con salt aleatoria por usuario

Esta decisión tecnológica se explica con más detalle (justificación
académica, arquitectura en capas y comparación de alternativas) en el
informe técnico de diseño que acompaña esta evidencia.

## 3. Estructura del proyecto

```
raspicholados-auth-api/
├── src/com/raspicholados/auth/
│   ├── Main.java                     # Punto de entrada, arranca el servidor
│   ├── modelo/Usuario.java           # Entidad de dominio
│   ├── repositorio/UsuarioRepositorio.java  # Persistencia
│   ├── servicio/AutenticacionServicio.java  # Lógica de negocio
│   ├── util/HashUtil.java            # Hashing seguro de contraseñas
│   ├── util/JsonUtil.java            # Utilidad mínima de JSON
│   └── web/
│       ├── RegistroHandler.java      # Controlador POST /api/registro
│       └── LoginHandler.java         # Controlador POST /api/login
├── build.sh / build.bat              # Compilación (Git Bash / Windows CMD)
├── run.sh / run.bat                  # Ejecución (Git Bash / Windows CMD)
└── .gitignore
```

## 4. Compilación y ejecución

**En Windows (CMD o doble clic):**
```bat
build.bat
run.bat
```

**En Git Bash, Linux o macOS:**
```bash
chmod +x build.sh run.sh
./build.sh
./run.sh
```

Se compila con `-encoding UTF-8` para que las tildes de los mensajes
("Autenticación exitosa") se vean correctamente también en Windows.

El servicio queda escuchando en `http://localhost:8080`.

## 5. Ejemplos de uso (curl)

**Registrar un usuario:**
```bash
curl -X POST http://localhost:8080/api/registro \
  -H "Content-Type: application/json" \
  -d '{"usuario":"vam","contrasena":"Cl4v3Segura!"}'
# Respuesta: 201 {"mensaje":"Usuario registrado exitosamente"}
```

**Iniciar sesión (correcto):**
```bash
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"usuario":"vam","contrasena":"Cl4v3Segura!"}'
# Respuesta: 200 {"mensaje":"Autenticación exitosa"}
```

**Iniciar sesión (credenciales incorrectas):**
```bash
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"usuario":"vam","contrasena":"incorrecta"}'
# Respuesta: 401 {"error":"Error en la autenticación"}
```

## 6. Repositorio de control de versiones

Este servicio forma parte del repositorio central del proyecto
Raspicholados, donde se integra como un módulo más junto a los de
inventario (Java/JDBC), web (Servlets y Spring Boot), front-end y móvil.

- Repositorio: https://github.com/Viarismon/Raspicholados-inventario
- Módulo: https://github.com/Viarismon/Raspicholados-inventario/tree/main/modulo-servicio-web-autenticacion
