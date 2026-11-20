# RASPICHOLADOS — Sistema de inventario para heladería

> **Estado actual (GA8-220501096-AA1-EV01, v1.1.0):** la aplicación web integrada está en
> [`modulo-servicios-web-inventario`](modulo-servicios-web-inventario/README.md): interfaz web
> (`web/`) + API REST (`src/`) + MySQL, con inicio de sesión JWT, roles y 65 pruebas unitarias
> JUnit (`test/`). Ejecutar con `build.bat` y `run.bat`, y abrir `http://localhost:8080/`.
>
> **Entrega GA8-220501096-AA1-EV02 (módulos integrados):** sitio publicado en
> https://viarismon.github.io/Raspicholados-inventario/ (prototipo, contrato de la API,
> reportes de prueba y paquete ejecutable) y
> [manual técnico](modulo-servicios-web-inventario/docs/MANUAL_TECNICO.md).

| Carpeta | Evidencia |
|---|---|
| `modulo-inventario-java` | AA2-EV01 / AA3 · Java + JDBC + MySQL (consola) |
| `modulo-web-servlets` | AA2-EV02 · Servlets / JSP |
| `modulo-web-spring` · `modulo-movil-android` | AA3-EV01 |
| `modulo-frontend-html-css-js` | AA4-EV03 · prototipo front-end (localStorage) |
| `modulo-servicio-web-autenticacion` | AA5-EV01 · servicio de registro y login |
| `modulo-servicios-web-inventario` | AA5-EV03 API REST · **GA8-AA1-EV01 aplicación web integrada** |

---

# RASPICHOLADOS — Módulo de Inventario (Java + JDBC)

Evidencia **GA7-220501096-AA2-EV01** — Codificación de módulos del software.

Módulo de inventario del sistema RASPICHOLADOS, codificado en Java con conexión
a base de datos MySQL mediante **JDBC**. Implementa las operaciones CRUD
(insertar, consultar, actualizar, eliminar) sobre la entidad `Producto`.

## Artefactos previos que sustentan esta codificación

- Informe técnico de plan de trabajo (tecnologías seleccionadas: MySQL como motor
  de base de datos).
- Informe de especificaciones funcionales y metodología — requisitos RF-01 a RF-05
  del módulo de inventario.
- Estándar de codificación del proyecto (nombramiento de variables, métodos,
  clases y paquetes).

## Estructura del proyecto

```
raspicholados-inventario-java/
├── sql/
│   └── raspicholados_inventario.sql   # Creación de BD y tabla + datos de prueba
├── src/co/sena/raspicholados/inventario/
│   ├── modelo/
│   │   └── Producto.java              # Clase de dominio
│   ├── conexion/
│   │   └── ConexionBD.java            # Conexión JDBC a MySQL
│   ├── dao/
│   │   ├── ProductoDAO.java           # Contrato CRUD (interfaz)
│   │   └── ProductoDAOImpl.java       # Implementación JDBC del CRUD
│   └── principal/
│       └── PruebaInventarioCRUD.java  # Clase main que evidencia el CRUD
└── lib/
    └── (aquí va mysql-connector-j-x.x.x.jar, no incluido en el repositorio)
```

## Estándares de nombramiento aplicados

| Elemento   | Convención          | Ejemplo                              |
|------------|----------------------|---------------------------------------|
| Paquetes   | minúsculas, dominio invertido | `co.sena.raspicholados.inventario.dao` |
| Clases     | PascalCase           | `Producto`, `ProductoDAOImpl`        |
| Interfaces | PascalCase           | `ProductoDAO`                        |
| Métodos    | camelCase, verbo + sustantivo | `insertarProducto`, `consultarProductoPorId` |
| Variables  | camelCase, descriptivas | `stockActual`, `idProducto`        |
| Constantes | UPPER_SNAKE_CASE     | `SQL_INSERTAR`, `URL_BASE_DATOS`     |

## Cómo ejecutarlo

1. Instalar MySQL y ejecutar el script `sql/raspicholados_inventario.sql`.
2. Descargar el driver JDBC **MySQL Connector/J** y colocarlo en la carpeta `lib/`.
3. Ajustar usuario y contraseña en `ConexionBD.java` si son distintos a los de tu equipo.
4. Compilar y ejecutar:

```
javac -d bin -cp lib/mysql-connector-j-8.x.x.jar $(find src -name "*.java")
java -cp "bin:lib/mysql-connector-j-8.x.x.jar" co.sena.raspicholados.inventario.principal.PruebaInventarioCRUD
```

(En Windows, reemplazar `:` por `;` en el `-cp`.)

## Control de versiones

Proyecto inicializado con Git. Para vincularlo a GitHub:

```
git remote add origin <URL_DE_TU_REPOSITORIO>
git push -u origin main
```

---
**Autora:** Viviana Arias Montilla · **Instructor:** William Fernando Muñoz · SENA 2026
