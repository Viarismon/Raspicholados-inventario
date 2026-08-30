# RASPICHOLADOS — Módulo de Inventario Web (Servlets + JSP)

Evidencia **GA7-220501096-AA2-EV02** — Módulos de software codificados y probados
(enfoque web con Servlets).

Capa de presentación web del módulo de inventario del sistema RASPICHOLADOS,
construida con **Servlets** y **JSP**, sobre la misma capa de lógica de negocio
y acceso a datos (DAO + JDBC) ya codificada y probada en la evidencia
GA7-220501096-AA2-EV01.

## Artefactos previos que sustentan esta codificación

- Informe de especificaciones funcionales y metodología (RF-01 a RF-05 del
  módulo de inventario).
- Informe de diseño de la solución de software (arquitectura en capas y
  patrón DAO ya definidos y validados).
- Evidencia EV01: clases `Producto`, `ProductoDAO`, `ProductoDAOImpl` y
  `ConexionBD`, reutilizadas sin cambios en este proyecto.

## Estructura del proyecto

```
raspicholados-web-servlets/
├── sql/
│   └── raspicholados_inventario.sql
├── src/co/sena/raspicholados/inventario/
│   ├── modelo/Producto.java                 # Reutilizado de EV01
│   ├── conexion/ConexionBD.java             # Reutilizado de EV01
│   ├── dao/ProductoDAO.java                 # Reutilizado de EV01
│   ├── dao/ProductoDAOImpl.java             # Reutilizado de EV01
│   └── servlet/ProductoServlet.java         # Nuevo: controlador web (doGet/doPost)
└── WebContent/
    ├── index.jsp                            # Redirección de bienvenida
    ├── listar_productos.jsp                 # Vista: listado (GET)
    ├── formulario_producto.jsp              # Vista: alta/edición (GET carga, POST guarda)
    ├── estilos.css
    └── WEB-INF/
        ├── web.xml
        └── lib/  (aquí va mysql-connector-j-x.x.x.jar, no incluido)
```

## Cómo funciona el flujo GET / POST

| Acción del usuario | Método HTTP | Ruta | Qué hace |
|---|---|---|---|
| Ver el inventario | GET | `/productos` | Lista todos los productos (`listar_productos.jsp`) |
| Abrir formulario de producto nuevo | GET | `/productos?accion=nuevo` | Muestra formulario vacío |
| Abrir formulario para editar | GET | `/productos?accion=editar&id=N` | Carga el producto N y lo muestra en el formulario |
| Eliminar un producto | GET | `/productos?accion=eliminar&id=N` | Elimina y redirige al listado |
| Guardar (nuevo o editado) | **POST** | `/productos` | Inserta o actualiza según venga `idProducto` |

Después de cada operación (`eliminar` o el `POST` del formulario), el servlet
usa `response.sendRedirect(...)` para volver al listado con una nueva petición
GET — patrón **Post/Redirect/Get**, que evita que recargar la página repita
la última operación.

## Elementos JSP utilizados

- Directivas de página: `<%@ page ... %>`, `<%@ page import="..." %>`
- Scriptlets: `<% ... %>` para recorrer la lista de productos y controlar
  el modo del formulario (crear/editar)
- Expresiones: `<%= ... %>`
- Acciones estándar: `<jsp:useBean>` y `<jsp:getProperty>` en el formulario
- Expression Language (EL): `${listaProductos.size()}` en el listado
- Comentarios JSP: `<%-- ... --%>`

## Cómo ejecutarlo

1. Ejecutar el script `sql/raspicholados_inventario.sql` en MySQL.
2. Descargar **MySQL Connector/J** y colocarlo en `WebContent/WEB-INF/lib/`.
3. Importar el proyecto como *Dynamic Web Project* en Eclipse (o equivalente)
   apuntando `src` como carpeta fuente y `WebContent` como raíz web.
4. Desplegar en Apache Tomcat (9 o 10, con namespace `javax.servlet`) y
   acceder a `http://localhost:8080/raspicholados-web-servlets/`.

## Control de versiones

Proyecto integrado al repositorio principal `Raspicholados-inventario` como
un módulo adicional, junto al módulo de escritorio (JDBC) de la evidencia EV01.

---
**Autora:** Viviana Arias Montilla · **Instructor:** William Fernando Muñoz · SENA 2026
