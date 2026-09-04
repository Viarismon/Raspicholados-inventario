# RASPICHOLADOS — Módulo de Inventario Móvil (Android)

Evidencia **GA7-220501096-AA3-EV01** — Codificación de módulos del software
stand-alone, web y móvil (enfoque móvil, framework Android/SQLite).

Este proyecto cierra la trilogía de codificación del módulo de inventario del
sistema RASPICHOLADOS, aplicando la misma arquitectura y el mismo patrón DAO
sobre tres tecnologías distintas:

| Evidencia | Enfoque | Persistencia | Estado |
|---|---|---|---|
| GA7-220501096-AA2-EV01 | Stand-alone (Java) | MySQL vía JDBC | ✅ Completada |
| GA7-220501096-AA2-EV02 | Web (Servlets + JSP) | MySQL vía JDBC | ✅ Completada |
| **GA7-220501096-AA3-EV01** | **Móvil (Android)** | **SQLite embebido** | ✅ Esta evidencia |

## Artefactos previos que sustentan esta codificación

- Informe de especificaciones funcionales y metodología (RF-01 a RF-05).
- Informe de diseño de la solución de software (arquitectura en capas y
  patrón DAO).
- Evidencias EV01 y EV02, de donde se reutiliza el diseño de la entidad
  `Producto` y la firma de los métodos del DAO (`insertarProducto`,
  `consultarProductoPorId`, `listarProductos`, `actualizarProducto`,
  `eliminarProducto`), ahora implementados sobre SQLite en vez de JDBC/MySQL.

## Por qué SQLite y no un servidor remoto

El proyecto aún no cuenta con una API REST publicada que la app pudiera
consumir. SQLite es la base de datos embebida estándar de Android: cada
instalación de la app mantiene su propio inventario local, sin depender de
conexión a internet ni de un servidor. Esta decisión se documenta como
supuesto de diseño para esta evidencia y puede evolucionar más adelante hacia
una arquitectura cliente-servidor si el proyecto lo requiere.

## Estructura del proyecto

```
raspicholados-movil-android/
├── build.gradle                        # Configuración a nivel de proyecto
├── settings.gradle
├── gradle.properties
└── app/
    ├── build.gradle                    # Dependencias del módulo app
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/co/sena/raspicholados/inventario/
        │   ├── modelo/Producto.java            # Entidad (Serializable)
        │   ├── db/ProductoDBHelper.java         # SQLiteOpenHelper
        │   ├── dao/ProductoDAO.java             # CRUD con SQLite
        │   ├── adapter/ProductoAdapter.java     # Adaptador RecyclerView
        │   └── ui/
        │       ├── MainActivity.java            # Listado de productos
        │       └── FormularioProductoActivity.java  # Alta / edición / eliminación
        └── res/
            ├── layout/       (3 pantallas)
            ├── values/       (strings, colors, themes)
            └── mipmap-*/     (ícono de la app)
```

## Funcionalidades

- Listado de productos del inventario (RecyclerView + tarjetas), con
  indicador visual de stock bajo (RF-04).
- Registrar un producto nuevo.
- Editar un producto existente (toca cualquier fila de la lista).
- Eliminar un producto, con diálogo de confirmación.
- Datos de ejemplo precargados la primera vez que se instala la app.

## Estándares de codificación aplicados

- Paquetes en minúsculas, dominio invertido: `co.sena.raspicholados.inventario.*`
- Clases en PascalCase: `MainActivity`, `ProductoDAO`, `ProductoDBHelper`
- Métodos y variables en camelCase, descriptivos: `listarProductos`,
  `campoStockActual`
- Constantes en UPPER_SNAKE_CASE: `NOMBRE_BASE_DATOS`, `TABLA_PRODUCTO`
- Todos los recursos de texto centralizados en `strings.xml` (ningún texto
  literal dentro de los layouts ni del código Java)
- Comentario de encabezado (módulo, propósito, autora, fecha) en cada archivo,
  y comentarios Javadoc en los métodos con lógica relevante

## Cómo ejecutarlo

1. Abrir la carpeta `raspicholados-movil-android` como proyecto en
   **Android Studio** (Hedgehog o superior).
2. Dejar que Gradle sincronice las dependencias (requiere conexión a
   internet la primera vez).
3. Ejecutar en un emulador o dispositivo físico con Android 5.0 (API 21)
   o superior.
4. La base de datos SQLite se crea automáticamente en el primer arranque,
   con tres productos de ejemplo.

## Control de versiones

Proyecto integrado al repositorio principal `Raspicholados-inventario`,
como módulo adicional junto a `modulo-inventario-java` (EV01) y
`modulo-web-servlets` (EV02).

---
**Autora:** Viviana Arias Montilla · **Instructor:** William Fernando Muñoz · SENA 2026
