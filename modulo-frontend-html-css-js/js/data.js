/* ============================================================================
   RASPICHOLADOS · Módulo de datos (data.js)
   ----------------------------------------------------------------------------
   Responsabilidad única: definir el modelo de datos del inventario y exponer
   funciones de acceso (CRUD) que persisten en localStorage del navegador.

   Este archivo NO manipula el DOM: separa la capa de datos de la capa de
   presentación (app.js), siguiendo el principio de responsabilidad única.
   ============================================================================ */

// Claves usadas en localStorage
const LS_KEY_PRODUCTOS = "raspicholados_productos";
const LS_KEY_MOVIMIENTOS = "raspicholados_movimientos";

/**
 * Datos semilla: se cargan solo la primera vez que se abre la aplicación,
 * para que el evaluador vea el sistema funcionando con información realista
 * de una heladería / raspadería.
 */
const PRODUCTOS_SEMILLA = [
  { id: "p1", nombre: "Raspado de mora",        categoria: "Raspados", unidad: "unidad", stock: 42, stockMinimo: 15, precio: 4500,  proveedor: "Frutas del Valle" },
  { id: "p2", nombre: "Raspado de mango",       categoria: "Raspados", unidad: "unidad", stock: 8,  stockMinimo: 15, precio: 4500,  proveedor: "Frutas del Valle" },
  { id: "p3", nombre: "Raspado de tamarindo",   categoria: "Raspados", unidad: "unidad", stock: 25, stockMinimo: 10, precio: 4800,  proveedor: "Frutas del Valle" },
  { id: "p4", nombre: "Helado de vainilla",     categoria: "Helados",  unidad: "litro",  stock: 12, stockMinimo: 5,  precio: 18000, proveedor: "Lácteos San Rafael" },
  { id: "p5", nombre: "Helado de chocolate",    categoria: "Helados",  unidad: "litro",  stock: 3,  stockMinimo: 5,  precio: 18500, proveedor: "Lácteos San Rafael" },
  { id: "p6", nombre: "Leche condensada",       categoria: "Toppings", unidad: "unidad", stock: 20, stockMinimo: 8,  precio: 6200,  proveedor: "Distribuidora Central" },
  { id: "p7", nombre: "Chocolate en polvo",     categoria: "Toppings", unidad: "kg",     stock: 6,  stockMinimo: 3,  precio: 15000, proveedor: "Distribuidora Central" },
  { id: "p8", nombre: "Vasos desechables 12oz", categoria: "Insumos",  unidad: "unidad", stock: 300,stockMinimo: 100,precio: 150,   proveedor: "Empaques del Sur" },
  { id: "p9", nombre: "Cucharas plásticas",     categoria: "Insumos",  unidad: "unidad", stock: 90, stockMinimo: 100,precio: 60,    proveedor: "Empaques del Sur" },
];

/* ----------------------------------------------------------------------- */
/* Inicialización                                                          */
/* ----------------------------------------------------------------------- */

function inicializarDatos() {
  if (localStorage.getItem(LS_KEY_PRODUCTOS) === null) {
    localStorage.setItem(LS_KEY_PRODUCTOS, JSON.stringify(PRODUCTOS_SEMILLA));
  }
  if (localStorage.getItem(LS_KEY_MOVIMIENTOS) === null) {
    localStorage.setItem(LS_KEY_MOVIMIENTOS, JSON.stringify([]));
  }
}

/* ----------------------------------------------------------------------- */
/* Utilidades internas                                                     */
/* ----------------------------------------------------------------------- */

function leerProductos() {
  return JSON.parse(localStorage.getItem(LS_KEY_PRODUCTOS) || "[]");
}

function guardarProductos(productos) {
  localStorage.setItem(LS_KEY_PRODUCTOS, JSON.stringify(productos));
}

function leerMovimientos() {
  return JSON.parse(localStorage.getItem(LS_KEY_MOVIMIENTOS) || "[]");
}

function guardarMovimientos(movimientos) {
  localStorage.setItem(LS_KEY_MOVIMIENTOS, JSON.stringify(movimientos));
}

function generarId(prefijo) {
  return prefijo + "_" + Date.now().toString(36) + Math.random().toString(36).slice(2, 6);
}

/* ----------------------------------------------------------------------- */
/* API pública: PRODUCTOS (CRUD)                                           */
/* ----------------------------------------------------------------------- */

const ProductosAPI = {
  listar() {
    return leerProductos();
  },

  obtener(id) {
    return leerProductos().find((p) => p.id === id) || null;
  },

  crear(datos) {
    const productos = leerProductos();
    const nuevo = { id: generarId("p"), ...datos };
    productos.push(nuevo);
    guardarProductos(productos);
    return nuevo;
  },

  actualizar(id, datos) {
    const productos = leerProductos();
    const idx = productos.findIndex((p) => p.id === id);
    if (idx === -1) return null;
    productos[idx] = { ...productos[idx], ...datos };
    guardarProductos(productos);
    return productos[idx];
  },

  eliminar(id) {
    const productos = leerProductos().filter((p) => p.id !== id);
    guardarProductos(productos);
  },

  /** Ajusta el stock de un producto sumando (o restando) una cantidad. */
  ajustarStock(id, cantidadDelta) {
    const productos = leerProductos();
    const idx = productos.findIndex((p) => p.id === id);
    if (idx === -1) return null;
    productos[idx].stock = Math.max(0, productos[idx].stock + cantidadDelta);
    guardarProductos(productos);
    return productos[idx];
  },
};

/* ----------------------------------------------------------------------- */
/* API pública: MOVIMIENTOS                                                */
/* ----------------------------------------------------------------------- */

const MovimientosAPI = {
  listar() {
    // Se muestran del más reciente al más antiguo
    return leerMovimientos().slice().reverse();
  },

  /**
   * Registra un movimiento de entrada o salida y actualiza el stock
   * del producto correspondiente en la misma operación (regla de negocio).
   */
  registrar({ productoId, tipo, cantidad, motivo }) {
    const producto = ProductosAPI.obtener(productoId);
    if (!producto) throw new Error("Producto no encontrado");

    const delta = tipo === "entrada" ? cantidad : -cantidad;
    ProductosAPI.ajustarStock(productoId, delta);

    const movimientos = leerMovimientos();
    const nuevo = {
      id: generarId("m"),
      productoId,
      productoNombre: producto.nombre,
      tipo,
      cantidad,
      motivo: motivo || "",
      fecha: new Date().toISOString(),
    };
    movimientos.push(nuevo);
    guardarMovimientos(movimientos);
    return nuevo;
  },
};
