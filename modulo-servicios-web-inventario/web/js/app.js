/* ============================================================================
   RASPICHOLADOS · Capa de presentación (app.js)
   ----------------------------------------------------------------------------
   Evidencia GA8-220501096-AA1-EV01 · Integración de módulos.

   Responsabilidad: navegación entre vistas, pintar datos en el DOM y atender
   eventos del usuario. Todos los datos llegan de la API a través de api.js
   (ProductosAPI, MovimientosAPI, ...); este archivo no usa fetch directamente.

   Cambios frente a la AA4-EV03:
   - Pantalla de inicio de sesión y cierre de sesión (JWT).
   - Interfaz según el rol: el EMPLEADO no ve crear/editar/dar de baja ni la
     vista de usuarios (la API también lo impide con 403).
   - Funciones asíncronas (async/await) porque los datos vienen de la red.
   - Todo texto que viene de la base de datos se escapa antes de insertarlo
     en el HTML, para evitar inyección de código (XSS).
   ============================================================================ */

/** Estado en memoria de la vista (caché de la última consulta). */
const estado = {
  productos: [],
  categorias: [],
  proveedores: [],
  vistaActual: "dashboard",
};

document.addEventListener("DOMContentLoaded", () => {
  configurarAlExpirarSesion(() => {
    mostrarLogin("Su sesión expiró. Ingrese nuevamente.");
  });

  inicializarLogin();
  inicializarNavegacion();
  inicializarModalProducto();
  inicializarFormularioMovimiento();
  inicializarFormularioUsuario();
  inicializarToolbarInventario();
  inicializarExportarCSV();

  if (Sesion.obtener()) {
    entrarALaAplicacion();
  } else {
    mostrarLogin();
  }
});

/* ----------------------------------------------------------------------- */
/* Utilidades                                                              */
/* ----------------------------------------------------------------------- */

/** Escapa texto para insertarlo de forma segura dentro de HTML. */
function escapar(valor) {
  return String(valor ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function formatearMoneda(valor) {
  return Number(valor || 0).toLocaleString("es-CO", { style: "currency", currency: "COP", maximumFractionDigits: 0 });
}

function formatearFecha(textoFecha) {
  const fecha = new Date(textoFecha);
  return isNaN(fecha) ? escapar(textoFecha) : fecha.toLocaleString("es-CO", { dateStyle: "short", timeStyle: "short" });
}

function mostrarToast(mensaje) {
  const toast = document.getElementById("toast");
  toast.textContent = mensaje;
  toast.hidden = false;
  clearTimeout(mostrarToast._t);
  mostrarToast._t = setTimeout(() => { toast.hidden = true; }, 3000);
}

function mostrarError(idElemento, error) {
  const elemento = document.getElementById(idElemento);
  elemento.textContent = error instanceof ErrorApi ? error.textoCompleto() : String(error.message || error);
  elemento.hidden = false;
}

function ocultarError(idElemento) {
  document.getElementById(idElemento).hidden = true;
}

const ETIQUETA_ESTADO = { NORMAL: "Normal", BAJO: "Stock bajo", AGOTADO: "Agotado" };

/* ----------------------------------------------------------------------- */
/* Inicio y cierre de sesión                                               */
/* ----------------------------------------------------------------------- */

function mostrarLogin(mensaje) {
  document.getElementById("app").hidden = true;
  document.getElementById("modal-overlay").hidden = true;
  document.getElementById("pantalla-login").hidden = false;
  document.getElementById("form-login").reset();
  if (mensaje) {
    mostrarError("login-error", mensaje);
  } else {
    ocultarError("login-error");
  }
  document.getElementById("login-usuario").focus();
}

function inicializarLogin() {
  document.getElementById("form-login").addEventListener("submit", async (e) => {
    e.preventDefault();
    const usuario = document.getElementById("login-usuario").value.trim();
    const contrasena = document.getElementById("login-contrasena").value;
    if (!usuario || !contrasena) {
      mostrarError("login-error", "Escriba el usuario y la contraseña");
      return;
    }
    const boton = document.getElementById("btn-login");
    boton.disabled = true;
    boton.textContent = "Verificando...";
    try {
      await AutenticacionAPI.iniciarSesion(usuario, contrasena);
      ocultarError("login-error");
      entrarALaAplicacion();
    } catch (error) {
      mostrarError("login-error", error);
    } finally {
      boton.disabled = false;
      boton.textContent = "Ingresar";
    }
  });

  document.getElementById("btn-salir").addEventListener("click", () => {
    AutenticacionAPI.cerrarSesion();
    mostrarLogin();
  });
}

async function entrarALaAplicacion() {
  const sesion = Sesion.obtener();
  document.getElementById("pantalla-login").hidden = true;
  document.getElementById("app").hidden = false;
  document.getElementById("usuario-nombre").textContent = sesion.usuario.nombreCompleto || sesion.usuario.nombreUsuario;
  document.getElementById("usuario-rol").textContent = sesion.usuario.rol === "ADMINISTRADOR" ? "Administrador" : "Empleado";
  // La clase es-empleado oculta en CSS todo lo marcado como .solo-admin
  document.body.classList.toggle("es-empleado", !Sesion.esAdministrador());
  await cargarCatalogos();
  cambiarVista("dashboard");
}

/* ----------------------------------------------------------------------- */
/* Navegación                                                              */
/* ----------------------------------------------------------------------- */

function inicializarNavegacion() {
  document.querySelectorAll(".nav-item").forEach((boton) => {
    boton.addEventListener("click", () => cambiarVista(boton.dataset.view));
  });
}

async function cambiarVista(nombreVista) {
  if (nombreVista === "usuarios" && !Sesion.esAdministrador()) nombreVista = "dashboard";
  estado.vistaActual = nombreVista;
  document.querySelectorAll(".nav-item").forEach((b) => b.classList.toggle("is-active", b.dataset.view === nombreVista));
  document.querySelectorAll(".view").forEach((v) => v.classList.toggle("is-active", v.id === `view-${nombreVista}`));
  await refrescarVista();
}

/** Consulta a la API solo lo que necesita la vista visible. */
async function refrescarVista() {
  try {
    switch (estado.vistaActual) {
      case "dashboard":
        await renderDashboard();
        break;
      case "inventario":
        estado.productos = await ProductosAPI.listar();
        renderTablaInventario();
        break;
      case "movimientos":
        estado.productos = await ProductosAPI.listar();
        renderSelectProductosMovimiento();
        await renderHistorialMovimientos();
        break;
      case "reportes":
        await renderResumenReporte();
        break;
      case "usuarios":
        await renderUsuarios();
        break;
    }
  } catch (error) {
    if (!(error instanceof ErrorApi && error.estado === 401)) {
      mostrarToast(error instanceof ErrorApi ? error.textoCompleto() : "Error al cargar los datos");
    }
  }
}

/** Categorías y proveedores alimentan los filtros y el formulario de producto. */
async function cargarCatalogos() {
  try {
    [estado.categorias, estado.proveedores] = await Promise.all([CategoriasAPI.listar(), ProveedoresAPI.listar()]);
  } catch (error) {
    estado.categorias = [];
    estado.proveedores = [];
  }
  const opcionesCategoria = estado.categorias.map((c) => `<option value="${escapar(c.nombre)}">${escapar(c.nombre)}</option>`).join("");
  document.getElementById("filtro-categoria").innerHTML = `<option value="todas">Todas las categorías</option>${opcionesCategoria}`;
  document.getElementById("prod-categoria").innerHTML = opcionesCategoria;
  document.getElementById("prod-proveedor").innerHTML = `<option value="">Sin proveedor</option>` +
    estado.proveedores.map((p) => `<option value="${p.id}">${escapar(p.nombre)}</option>`).join("");
}

/* ----------------------------------------------------------------------- */
/* PANEL GENERAL                                                           */
/* ----------------------------------------------------------------------- */

async function renderDashboard() {
  const [resumen, alertas] = await Promise.all([ReportesAPI.resumen(), ReportesAPI.alertas()]);

  document.getElementById("kpi-total-productos").textContent = resumen.totalProductosActivos;
  document.getElementById("kpi-total-unidades").textContent = resumen.totalUnidadesEnStock;
  document.getElementById("kpi-stock-bajo").textContent = resumen.productosAgotados + resumen.productosStockBajo;
  document.getElementById("kpi-valor-inventario").textContent = formatearMoneda(resumen.valorTotalInventario);

  const porCategoria = resumen.productosPorCategoria || {};
  const nombres = Object.keys(porCategoria);
  const maximo = Math.max(1, ...nombres.map((n) => porCategoria[n]));
  document.getElementById("chart-categorias").innerHTML = nombres.length
    ? nombres.map((nombre) => `
      <div class="bar-row">
        <span>${escapar(nombre)}</span>
        <span class="bar-track"><span class="bar-fill" data-porcentaje="${Math.round((porCategoria[nombre] / maximo) * 100)}"></span></span>
        <span>${Number(porCategoria[nombre])}</span>
      </div>`).join("")
    : `<p class="alert-empty">Aún no hay productos registrados.</p>`;
  // El ancho se asigna desde JavaScript (no con style="" en el HTML) para cumplir la política CSP.
  document.querySelectorAll("#chart-categorias .bar-fill").forEach((barra) => {
    barra.style.width = barra.dataset.porcentaje + "%";
  });

  document.getElementById("lista-alertas").innerHTML = alertas.length
    ? alertas.map((a) => `
      <li class="alert-item">
        <span>${escapar(a.nombre)} <small class="badge ${a.estadoStock === "AGOTADO" ? "badge-bajo" : "badge-alerta"}">${escapar(ETIQUETA_ESTADO[a.estadoStock] || a.estadoStock)}</small></span>
        <strong>${Number(a.stockActual)} / ${Number(a.stockMinimo)} ${escapar(a.unidadMedida)} · pedir ${Number(a.cantidadSugeridaPedido)}</strong>
      </li>`).join("")
    : `<li class="alert-empty">No hay productos por debajo del stock mínimo.</li>`;
}

/* ----------------------------------------------------------------------- */
/* INVENTARIO                                                              */
/* ----------------------------------------------------------------------- */

function inicializarToolbarInventario() {
  document.getElementById("buscador").addEventListener("input", renderTablaInventario);
  document.getElementById("filtro-categoria").addEventListener("change", renderTablaInventario);
  document.getElementById("filtro-estado").addEventListener("change", renderTablaInventario);
}

function renderTablaInventario() {
  const texto = document.getElementById("buscador").value.trim().toLowerCase();
  const categoria = document.getElementById("filtro-categoria").value;
  const estadoStock = document.getElementById("filtro-estado").value;

  const productos = estado.productos.filter((p) =>
    (!texto || p.nombre.toLowerCase().includes(texto)) &&
    (categoria === "todas" || p.categoria === categoria) &&
    (estadoStock === "todos" || p.estadoStock === estadoStock));

  const tbody = document.getElementById("tbody-inventario");
  document.getElementById("tabla-vacia").hidden = productos.length > 0;

  tbody.innerHTML = productos.map((p) => {
    const clase = p.estadoStock === "NORMAL" ? "badge-ok" : p.estadoStock === "BAJO" ? "badge-alerta" : "badge-bajo";
    return `
      <tr>
        <td>${escapar(p.nombre)}</td>
        <td>${escapar(p.categoria)}</td>
        <td>${escapar(p.unidadMedida)}</td>
        <td>${Number(p.stockActual)}</td>
        <td>${Number(p.stockMinimo)}</td>
        <td>${formatearMoneda(p.precioUnitario)}</td>
        <td>${escapar(p.proveedorNombre || "—")}</td>
        <td><span class="badge ${clase}">${escapar(ETIQUETA_ESTADO[p.estadoStock] || p.estadoStock)}</span></td>
        <td class="row-actions solo-admin"><button class="icon-btn" data-editar="${Number(p.id)}">Editar</button></td>
      </tr>`;
  }).join("");

  tbody.querySelectorAll("[data-editar]").forEach((btn) => {
    btn.addEventListener("click", () => abrirModalProducto(Number(btn.dataset.editar)));
  });
}

/* ----------------------------------------------------------------------- */
/* MODAL: crear / editar / dar de baja producto (solo administrador)      */
/* ----------------------------------------------------------------------- */

function inicializarModalProducto() {
  const overlay = document.getElementById("modal-overlay");
  document.getElementById("btn-nuevo-producto").addEventListener("click", () => abrirModalProducto(null));
  document.getElementById("btn-nuevo-producto-dash").addEventListener("click", () => abrirModalProducto(null));
  document.getElementById("btn-cerrar-modal").addEventListener("click", cerrarModalProducto);
  document.getElementById("btn-cancelar-modal").addEventListener("click", cerrarModalProducto);
  overlay.addEventListener("click", (e) => { if (e.target === overlay) cerrarModalProducto(); });
  document.addEventListener("keydown", (e) => { if (e.key === "Escape" && !overlay.hidden) cerrarModalProducto(); });

  document.getElementById("form-producto").addEventListener("submit", async (e) => {
    e.preventDefault();
    await guardarProductoDesdeFormulario();
  });

  document.getElementById("btn-eliminar-producto").addEventListener("click", async () => {
    const id = document.getElementById("prod-id").value;
    if (!id || !confirm("¿Dar de baja este producto? Dejará de aparecer en el inventario, pero su historial se conserva.")) return;
    try {
      await ProductosAPI.darDeBaja(id);
      cerrarModalProducto();
      mostrarToast("Producto dado de baja");
      await refrescarVista();
    } catch (error) {
      mostrarError("prod-error", error);
    }
  });
}

async function abrirModalProducto(id) {
  if (!Sesion.esAdministrador()) return;
  const form = document.getElementById("form-producto");
  form.reset();
  ocultarError("prod-error");
  const esNuevo = id === null;
  document.getElementById("modal-titulo").textContent = esNuevo ? "Nuevo producto" : "Editar producto";
  document.getElementById("btn-eliminar-producto").hidden = esNuevo;
  document.getElementById("campo-stock-inicial").hidden = !esNuevo;
  document.getElementById("nota-stock").hidden = esNuevo;
  document.getElementById("prod-id").value = esNuevo ? "" : id;

  if (!esNuevo) {
    try {
      const p = await ProductosAPI.obtener(id);
      document.getElementById("prod-nombre").value = p.nombre;
      document.getElementById("prod-categoria").value = p.categoria;
      document.getElementById("prod-unidad").value = p.unidadMedida;
      document.getElementById("prod-stock-min").value = p.stockMinimo;
      document.getElementById("prod-precio").value = p.precioUnitario;
      document.getElementById("prod-proveedor").value = p.proveedorId || "";
    } catch (error) {
      mostrarToast(error instanceof ErrorApi ? error.textoCompleto() : "No se pudo cargar el producto");
      return;
    }
  }
  document.getElementById("modal-overlay").hidden = false;
  document.getElementById("prod-nombre").focus();
}

function cerrarModalProducto() {
  document.getElementById("modal-overlay").hidden = true;
}

/** Arma el JSON con los nombres de campo que espera la API (PRO-03 / PRO-04). */
async function guardarProductoDesdeFormulario() {
  const id = document.getElementById("prod-id").value;
  const proveedor = document.getElementById("prod-proveedor").value;
  const datos = {
    nombre: document.getElementById("prod-nombre").value.trim(),
    categoria: document.getElementById("prod-categoria").value,
    unidadMedida: document.getElementById("prod-unidad").value.trim(),
    stockMinimo: Number(document.getElementById("prod-stock-min").value),
    precioUnitario: Number(document.getElementById("prod-precio").value),
  };
  if (proveedor) datos.proveedorId = Number(proveedor);
  const stockInicial = document.getElementById("prod-stock").value;
  if (!id && stockInicial !== "") datos.stockInicial = Number(stockInicial);

  try {
    if (id) {
      await ProductosAPI.actualizar(id, datos);
      mostrarToast("Producto actualizado");
    } else {
      await ProductosAPI.crear(datos);
      mostrarToast("Producto creado");
    }
    cerrarModalProducto();
    await refrescarVista();
  } catch (error) {
    // La validación definitiva la hace la API: aquí solo se muestran sus mensajes.
    mostrarError("prod-error", error);
  }
}

/* ----------------------------------------------------------------------- */
/* MOVIMIENTOS                                                             */
/* ----------------------------------------------------------------------- */

function renderSelectProductosMovimiento() {
  const select = document.getElementById("mov-producto");
  const seleccionActual = select.value;
  select.innerHTML = estado.productos.map((p) =>
    `<option value="${Number(p.id)}">${escapar(p.nombre)} (stock: ${Number(p.stockActual)} ${escapar(p.unidadMedida)})</option>`).join("");
  if (seleccionActual) select.value = seleccionActual;
}

function inicializarFormularioMovimiento() {
  document.getElementById("form-movimiento").addEventListener("submit", async (e) => {
    e.preventDefault();
    ocultarError("mov-error");
    const productoId = Number(document.getElementById("mov-producto").value);
    const tipo = document.getElementById("mov-tipo").value;
    const cantidad = Number(document.getElementById("mov-cantidad").value);
    const motivo = document.getElementById("mov-motivo").value.trim();

    if (!productoId || !Number.isInteger(cantidad) || cantidad <= 0) {
      mostrarError("mov-error", "Seleccione un producto y una cantidad entera mayor que cero");
      return;
    }
    try {
      // La regla "no sacar más de lo disponible" la garantiza la API (409 STOCK_INSUFICIENTE).
      const resultado = await MovimientosAPI.registrar({ productoId, tipo, cantidad, motivo: motivo || undefined });
      e.target.reset();
      mostrarToast(resultado.alerta || "Movimiento registrado");
      await refrescarVista();
    } catch (error) {
      mostrarError("mov-error", error);
    }
  });
}

async function renderHistorialMovimientos() {
  const movimientos = await MovimientosAPI.listar(25);
  const tbody = document.getElementById("tbody-movimientos");
  if (movimientos.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" class="tabla-vacia">Aún no se han registrado movimientos.</td></tr>`;
    return;
  }
  tbody.innerHTML = movimientos.map((m) => `
    <tr>
      <td>${formatearFecha(m.fecha)}</td>
      <td>${escapar(m.productoNombre)}</td>
      <td><span class="badge ${m.tipo === "ENTRADA" ? "badge-ok" : "badge-bajo"}">${escapar(m.tipo)}</span></td>
      <td>${Number(m.cantidad)}</td>
      <td>${Number(m.stockResultante)}</td>
      <td>${escapar(m.usuarioNombre || "—")}</td>
      <td>${escapar(m.motivo || "—")}</td>
    </tr>`).join("");
}

/* ----------------------------------------------------------------------- */
/* REPORTES                                                                */
/* ----------------------------------------------------------------------- */

async function renderResumenReporte() {
  const [resumen, alertas] = await Promise.all([ReportesAPI.resumen(), ReportesAPI.alertas()]);
  const nombres = alertas.map((a) => a.nombre).join(", ");
  document.getElementById("resumen-texto").textContent =
    `Con corte a ${formatearFecha(resumen.fechaCorte)}, el inventario cuenta con ${resumen.totalProductosActivos} ` +
    `productos activos y ${resumen.totalUnidadesEnStock} unidades, con un valor estimado de ` +
    `${formatearMoneda(resumen.valorTotalInventario)}. Hay ${resumen.productosAgotados} producto(s) agotado(s) y ` +
    `${resumen.productosStockBajo} con stock bajo${nombres ? ": " + nombres : ""}. Hoy se registraron ` +
    `${resumen.movimientosHoy} movimiento(s): ${resumen.unidadesEntradasHoy} unidades de entrada y ` +
    `${resumen.unidadesSalidasHoy} de salida.`;
}

function inicializarExportarCSV() {
  document.getElementById("btn-exportar-csv").addEventListener("click", async () => {
    try {
      const productos = await ProductosAPI.listar();
      const encabezado = ["Nombre", "Categoria", "Unidad", "Stock", "StockMinimo", "Estado", "PrecioUnitario", "Proveedor"];
      const filas = productos.map((p) =>
        [p.nombre, p.categoria, p.unidadMedida, p.stockActual, p.stockMinimo, p.estadoStock, p.precioUnitario, p.proveedorNombre || ""]
          // Se antepone ' a valores que empiezan por = + - @ para evitar inyección de fórmulas en Excel.
          .map((v) => String(v ?? "").replace(/^([=+\-@])/, "'$1"))
          .map((v) => `"${v.replace(/"/g, '""')}"`).join(","));
      const csv = "﻿" + [encabezado.join(","), ...filas].join("\r\n");
      const url = URL.createObjectURL(new Blob([csv], { type: "text/csv;charset=utf-8;" }));
      const enlace = document.createElement("a");
      enlace.href = url;
      enlace.download = "raspicholados_inventario.csv";
      enlace.click();
      URL.revokeObjectURL(url);
    } catch (error) {
      mostrarToast("No se pudo generar el CSV");
    }
  });
}

/* ----------------------------------------------------------------------- */
/* USUARIOS (solo administrador)                                          */
/* ----------------------------------------------------------------------- */

function inicializarFormularioUsuario() {
  document.getElementById("form-usuario").addEventListener("submit", async (e) => {
    e.preventDefault();
    ocultarError("usu-error");
    const datos = {
      nombreUsuario: document.getElementById("usu-nombre-usuario").value.trim(),
      nombreCompleto: document.getElementById("usu-nombre-completo").value.trim(),
      contrasena: document.getElementById("usu-contrasena").value,
      rol: document.getElementById("usu-rol").value,
    };
    try {
      await UsuariosAPI.registrar(datos);
      e.target.reset();
      mostrarToast("Usuario registrado");
      await renderUsuarios();
    } catch (error) {
      mostrarError("usu-error", error);
    }
  });
}

async function renderUsuarios() {
  const usuarios = await UsuariosAPI.listar();
  document.getElementById("tbody-usuarios").innerHTML = usuarios.map((u) => `
    <tr>
      <td>${escapar(u.nombreUsuario)}</td>
      <td>${escapar(u.nombreCompleto)}</td>
      <td>${u.rol === "ADMINISTRADOR" ? "Administrador" : "Empleado"}</td>
      <td><span class="badge ${u.activo ? "badge-ok" : "badge-bajo"}">${u.activo ? "Activo" : "Inactivo"}</span></td>
      <td>${formatearFecha(u.fechaRegistro)}</td>
    </tr>`).join("");
}
