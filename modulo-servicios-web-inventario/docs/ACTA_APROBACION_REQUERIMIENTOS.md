# Acta de aprobación de requerimientos y de entrega de módulos integrados

**Proyecto:** Sistema de inventario para la heladería RASPICHOLADOS
**Evidencia:** GA8-220501096-AA1-EV02 — Módulos integrados
**Versión entregada:** 1.1.0 (etiqueta Git `v1.1.0-ga8`; documentación de entrega `v1.1.1-ga8-ev02`)
**Repositorio:** https://github.com/Viarismon/Raspicholados-inventario

| Campo | Valor |
|---|---|
| Acta n.º | 01 |
| Fecha | ____ / ____ / 2026 |
| Lugar o medio | ______________________________ |
| Elaboró | Viviana Arias Montilla (aprendiz, desarrolladora) |
| Revisa | William Fernando Muñoz (instructor) |
| Aprueba por el negocio | ______________________________ (representante de Raspicholados) |

## 1. Objeto

Dejar constancia de los requerimientos funcionales acordados para el sistema (RF-01 a RF-15,
historias HU-01 a HU-14), del estado en que se entrega cada uno en la versión 1.1.0 y de
los compromisos para las siguientes versiones.

## 2. Requerimientos y estado de la entrega

Estados: **Cumple** (implementado y probado), **Parcial** (implementado con alcance menor
al acordado), **Pendiente** (planificado para una versión posterior).

| RF | HU | Requerimiento | Estado v1.1.0 | Evidencia (servicio / prueba) | Aprobado (Sí/No) |
|---|---|---|---|---|---|
| RF-01 | HU-01 | Registrar un producto nuevo | Cumple | PRO-03 · `ProductoServicioTest` · Newman | |
| RF-02 | HU-02 | Registrar entradas de inventario | Cumple | MOV-01 (ENTRADA) · `MovimientoServicioTest` | |
| RF-03 | HU-03 | Descontar stock al vender | Parcial: salida manual con motivo, sin módulo de ventas | MOV-01 (SALIDA) | |
| RF-04 | HU-04 | Alerta de producto por agotarse | Cumple | REP-01 · panel general | |
| RF-05 | HU-05 | Consultar existencias | Cumple | PRO-01, PRO-02 · buscador | |
| RF-06 | HU-06 | Registrar venta con forma de pago | Pendiente | — | |
| RF-07 | HU-07 | Generar comprobante de venta | Pendiente | — | |
| RF-08 | HU-03 | Impedir vender más de lo disponible | Cumple | 409 `STOCK_INSUFICIENTE` · `MovimientoServicioTest` | |
| RF-09 | HU-08 | Historial de ventas por fecha | Parcial: historial de movimientos con filtro de fechas | MOV-02 (`desde`, `hasta`) | |
| RF-10 | HU-09 | Iniciar sesión con usuario y contraseña | Cumple | AUT-01 · `AutenticacionServicioTest` | |
| RF-11 | HU-10 | Permisos por rol | Cumple | 403 `PERMISO_DENEGADO` · interfaz oculta funciones | |
| RF-12 | HU-11 | Registrar qué usuario hizo cada movimiento | Cumple | `movimiento_inventario.id_usuario` | |
| RF-13 | HU-12 | Reporte de inventario por categoría | Parcial: resumen por categoría y filtro | REP-02 · vista Reportes | |
| RF-14 | HU-13 | Reporte de ventas por periodo | Pendiente | — | |
| RF-15 | HU-14 | Exportar reportes a PDF o Excel | Parcial: exportación CSV (abre en Excel) | Vista Reportes | |

Resumen: 8 cumplen, 4 parciales y 3 pendientes (los pendientes forman el módulo de ventas,
sprint 2).

## 3. Productos entregados

- Código fuente en Git (rama `main`, etiquetas por versión).
- Archivos compilados: `dist/raspicholados-inventario.jar` y paquete `raspicholados-inventario-v1.1.0.zip`.
- URL: sitio de GitHub Pages y URL locales de la aplicación (ver informe y manual técnico).
- Documentación: informe GA8-AA1-EV02, `docs/MANUAL_TECNICO.md`, `docs/API.md`, `docs/openapi.json`.
- Pruebas: 65 pruebas JUnit y 205 aserciones Newman, ambas sin fallos.

## 4. Observaciones y compromisos

| # | Observación | Responsable | Fecha compromiso |
|---|---|---|---|
| 1 | Construir el módulo de ventas (RF-03 completo, RF-06, RF-07, RF-09, RF-14) | Desarrolladora | |
| 2 | Exportación a PDF y Excel nativo (RF-15) | Desarrolladora | |
| 3 | Publicar la aplicación integrada en un servidor con HTTPS | Desarrolladora | |
| 4 | | | |

## 5. Firmas

| Rol | Nombre | Firma | Fecha |
|---|---|---|---|
| Aprendiz / desarrolladora | Viviana Arias Montilla | | |
| Instructor | William Fernando Muñoz | | |
| Representante de Raspicholados | | | |
