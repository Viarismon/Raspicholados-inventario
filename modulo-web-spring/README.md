# RASPICHOLADOS — Módulo de Inventario Web con Framework (Spring Boot)

Evidencia **GA7-220501096-AA3-EV01** — Codificación de módulos del software
stand-alone, web y móvil (enfoque web, aplicando un framework).

Complementa al módulo Android de esta misma evidencia: mientras aquel cubre
el enfoque **móvil**, este proyecto cubre el enfoque **web**, aplicando un
framework real (Spring Boot + Spring Data JPA + Thymeleaf), distinto de los
Servlets/JSP "a mano" ya codificados en la evidencia EV02.

## Las cuatro implementaciones del mismo módulo, para comparar

| Evidencia | Enfoque | Persistencia | ¿Framework? |
|---|---|---|---|
| AA2-EV01 | Stand-alone (Java) | MySQL vía JDBC manual | No — JDBC puro |
| AA2-EV02 | Web (Servlets + JSP) | MySQL vía JDBC manual | No — Servlet API base |
| AA3-EV01 (parte 1) | Móvil (Android) | SQLite embebido | Android SDK |
| **AA3-EV01 (parte 2, este proyecto)** | **Web** | **MySQL vía Spring Data JPA** | **Sí — Spring Boot** |

## Qué cambia al usar un framework (Spring Boot) frente a Servlets "a mano"

- **Sin DAO manual:** `ProductoRepository` extiende `JpaRepository` y el
  framework genera automáticamente las sentencias SQL de insertar,
  consultar, listar y eliminar — no se escribe una sola línea de SQL a mano
  (a diferencia de `ProductoDAOImpl` en EV01/EV02).
- **Sin doGet/doPost manuales:** el controlador usa anotaciones
  (`@GetMapping`, `@PostMapping`) y Spring enruta las peticiones
  automáticamente según la URL y el método HTTP.
- **Sin scriptlets en las vistas:** Thymeleaf reemplaza los `<% %>` de JSP
  por atributos declarativos (`th:each`, `th:text`, `th:field`), separando
  mejor la lógica de la presentación.
- **Inyección de dependencias:** el controlador y el servicio reciben sus
  dependencias por el constructor (`@Autowired`), en vez de instanciarlas
  manualmente con `new`.

## Artefactos previos que sustentan esta codificación

- Informe de especificaciones funcionales (RF-01 a RF-05).
- Informe de diseño de la solución de software (arquitectura en capas,
  ahora con Spring gestionando la inyección entre capas).
- Evidencias EV01 y EV02: mismo diseño de la entidad `Producto` y mismo
  esquema de base de datos (`sql/raspicholados_inventario.sql`, reutilizado
  sin cambios).

## Estructura del proyecto

```
raspicholados-web-spring/
├── pom.xml
├── sql/raspicholados_inventario.sql        # Reutilizado de EV01/EV02
└── src/main/
    ├── java/co/sena/raspicholados/inventario/
    │   ├── RaspicholadosWebSpringApplication.java   # Arranque (@SpringBootApplication)
    │   ├── modelo/Producto.java                      # Entidad JPA
    │   ├── repositorio/ProductoRepository.java        # CRUD generado por Spring Data JPA
    │   ├── servicio/ProductoService.java               # Lógica de negocio
    │   └── controlador/ProductoController.java          # Rutas GET/POST
    └── resources/
        ├── application.properties           # Conexión a MySQL, puerto 8081
        ├── templates/
        │   ├── listar_productos.html         # Vista Thymeleaf
        │   └── formulario_producto.html      # Vista Thymeleaf
        └── static/estilos.css                # Reutilizado de EV02
```

## Cómo funciona el flujo GET / POST

| Acción | Método | Ruta |
|---|---|---|
| Ver el inventario | GET | `/productos` |
| Formulario de producto nuevo | GET | `/productos/nuevo` |
| Formulario de edición | GET | `/productos/editar/{id}` |
| Eliminar | GET | `/productos/eliminar/{id}` |
| Guardar (nuevo o editado) | **POST** | `/productos/guardar` |

## Cómo ejecutarlo

1. Ejecutar el script `sql/raspicholados_inventario.sql` en MySQL (o dejar
   que Spring Boot cree la tabla automáticamente con
   `spring.jpa.hibernate.ddl-auto=update`).
2. Abrir la carpeta como proyecto Maven en **IntelliJ IDEA**, **Eclipse
   (con plugin Spring)** o **Spring Tool Suite**.
3. Ejecutar la clase `RaspicholadosWebSpringApplication` (botón ▶ o
   `mvn spring-boot:run`).
4. Abrir `http://localhost:8081/productos` en el navegador.

## Control de versiones

Proyecto integrado al repositorio principal `Raspicholados-inventario`,
como módulo adicional junto a `modulo-inventario-java` (EV01),
`modulo-web-servlets` (EV02) y `modulo-movil-android` (AA3-EV01, parte móvil).

---
**Autora:** Viviana Arias Montilla · **Instructor:** William Fernando Muñoz · SENA 2026
