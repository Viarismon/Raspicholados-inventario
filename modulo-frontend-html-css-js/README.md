# Raspicholados · Sistema de Inventario (Front-end)

Módulo front-end del proyecto formativo **Construcción de software integrador
de tecnologías orientadas a servicios**, desarrollado para la heladería
**Raspicholados**.

**Evidencia:** GA7-220501096-AA4-EV03 — Componente front-end del proyecto formativo.

## Descripción

Aplicación web de una sola página (SPA) construida en **HTML, CSS y
JavaScript puro** (sin frameworks ni librerías externas) que permite:

- Ver un **panel general (dashboard)** con indicadores clave del inventario:
  total de productos, unidades en stock, productos por debajo del stock
  mínimo y valor total del inventario.
- **Gestionar el inventario (CRUD)**: crear, consultar, editar y eliminar
  productos (raspados, helados, toppings e insumos), con buscador y filtros
  por categoría y por estado de stock.
- **Registrar movimientos** de entrada y salida de stock, con actualización
  automática de existencias y validación de stock disponible.
- **Generar reportes**: resumen ejecutivo del estado del inventario y
  exportación a CSV.

## Arquitectura del código

```
raspicholados-inventario/
├── index.html          # Estructura de la SPA (dashboard, inventario, movimientos, reportes)
├── css/
│   └── styles.css      # Identidad visual e interfaz responsiva
├── js/
│   ├── data.js         # Capa de datos: modelo, persistencia (localStorage) y API CRUD
│   └── app.js           # Capa de presentación: navegación, render y eventos del DOM
└── README.md
```

El código separa **datos** (`data.js`) de **presentación** (`app.js`),
siguiendo el principio de responsabilidad única: `data.js` no toca el DOM y
`app.js` no accede directamente a `localStorage`, sino a través de
`ProductosAPI` y `MovimientosAPI`.

## Persistencia

Los datos se almacenan en el `localStorage` del navegador, por lo que el
sistema conserva la información entre sesiones sin necesidad de un servidor.
Al abrir la aplicación por primera vez se cargan datos de ejemplo
representativos de una raspadería/heladería.

> **Nota de alcance:** este componente front-end es independiente del
> backend en Java/JDBC ya construido en la evidencia
> GA7-220501096-AA3-EV01. Esta decisión se documenta y se analiza
> críticamente en el `Informe_Prototipos_Maquetacion.docx` (sección 8):
> permite entregar un prototipo autocontenido y evaluable sin depender de
> un servidor activo, quedando como trabajo futuro reemplazar `js/data.js`
> por llamadas a la API real, sin modificar `app.js`.

## Estándares de codificación aplicados

- Nomenclatura consistente en español (camelCase para variables/funciones,
  kebab-case para clases CSS e ids).
- Comentarios de cabecera por archivo y por bloque funcional.
- Separación de responsabilidades (datos / presentación / estilos).
- HTML semántico (`header`, `main`, `nav`, `aside`, `section`, `form`).
- Accesibilidad básica: etiquetas `label`, atributos `aria-*`, foco visible.
- Diseño responsivo (mobile-first en breakpoints principales).

## Cómo ejecutar

No requiere instalación ni dependencias. Basta con abrir `index.html` en
cualquier navegador moderno, o servirlo con un servidor estático local, por
ejemplo:

```bash
npx serve .
# o
python3 -m http.server 8080
```

## Control de versiones

El proyecto se gestiona con **Git**. Consulte el historial de commits para
ver la evolución del desarrollo del módulo.

## Autor

Aprendiz — Tecnología en Análisis y Desarrollo de Software (ADSO).
Ficha: [completar]. Instructor: [completar].
