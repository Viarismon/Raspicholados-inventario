/* ============================================================================
   RASPICHOLADOS · Módulo de interfaz (app.js)
   ----------------------------------------------------------------------------
   Responsabilidad: manejar la navegación entre vistas, pintar los datos en el
   DOM y responder a los eventos del usuario. Toda la lógica de datos vive en
   data.js (ProductosAPI / MovimientosAPI); este archivo solo consume esa API.
   ============================================================================ */

document.addEventListener("DOMContentLoaded", () => {
  inicializarDatos();       // definido en data.js
  inicializarNavegacion();
  inicializarModalProducto();
  inicializarFormularioMovimiento();
  inicializarToolbarInventario();
  inicializarExportarCSV();

  renderTodo(); // primer pintado de la aplicación
});

/* ----------------------------------------------------------------------- */
/* Utilidades de formato                                                   */
/* ----------------------------------------------------------------------- */

function formatearMoneda(valor) {
  return valor.toLocaleString("es-CO", { style: "currency", currency: "COP", maximumFractionDigits: 0 });
}

function formatearFecha(isoString) {
  const fecha = new Date(isoString);
  return fecha.toLocaleString("es-CO", { dateStyle: "short", timeStyle: "short" });
}

function mostrarToast(mensaje) {
  const toast = document.getElementById("toast");
  toast.textContent = mensaje;
  toast.hidden = false;
  clearTimeout(mostrarToast._t);
  mostrarToast._t = setTimeout(() => { toast.hidden = true; }, 2200);
}

/* ----------------------------------------------------------------------- */
/* Navegación entre vistas                                                 */
/* ----------------------------------------------------------------------- */

function inicializarNavegacion() {
  const botones = document.querySelectorAll(".nav-item");
  botones.forEach((boton) => {
    boton.addEventListener("click", () => cambiarVista(boton.dataset.view));
  });
}

function cambiarVista(nombreVista) {
  document.querySelectorAll(".nav-item").forEach((b) => {
    b.classList.toggle("is-active", b.dataset.view === nombreVista);
  });
  document.querySelectorAll(".view").forEach((v) => {
    v.classList.toggle("is-active", v.id === `view-${nombreVista}`);
  });
  renderTodo(); // se refresca la información cada vez que se cambia de vista
}

/* ----------------------------------------------------------------------- */
/* Render general                                                          */
/* ----------------------------------------------------------------------- */

function renderTodo() {
  renderDashboard();
  renderTablaInventario();
  renderSelectProductosMovimiento();
  renderHistorialMovimientos();
  renderResumenReporte();
}

/* ----------------------------------------------------------------------- */
/* DASHBOARD                                                               */
/* ----------------------------------------------------------------------- */

function renderDashboard() {
  const productos = ProductosAPI.listar();

  const totalProductos = productos.length;
  const totalUnidades = productos.reduce((acc, p) => acc + p.stock, 0);
  const stockBajo = productos.filter((p) => p.stock < p.stockMinimo);
  const valorInventario = productos.reduce((acc, p) => acc + p.stock * p.precio, 0);

  document.getElementById("kpi-total-productos").textContent = totalProductos;
  document.getElementById("kpi-total-unidades").textContent = totalUnidades;
  document.getElementById("kpi-stock-bajo").textContent = stockBajo.length;
  document.getElementById("kpi-valor-inventario").textContent = formatearMoneda(valorInventario);

  // Gráfico de barras simple (sin librerías externas) por categoría
  const categorias = ["Raspados", "Helados", "Toppings", "Insumos"];
  const maxUnidades = Math.max(1, ...categorias.map((c) =>
    productos.filter((p) => p.categoria === c).reduce((acc, p) => acc + p.stock, 0)
  ));

  const contenedorChart = document.getElementById("chart-categorias");
  contenedorChart.innerHTML = categorias.map((cat) => {
    const total = productos.filter((p) => p.categoria === cat).reduce((acc, p) => acc + p.stock, 0);
    const porcentaje = Math.round((total / maxUnidades) * 100);
    return `
      <div class="bar-row">
        <span>${cat}</span>
        <span class="bar-track"><span class="bar-fill" style="width:${porcentaje}%"></span></span>
        <span>${total}</span>
      </div>`;
  }).join("");

  // Alertas de stock bajo mínimo
  const listaAlertas = document.getElementById("lista-alertas");
  if (stockBajo.length === 0) {
    listaAlertas.innerHTML = `<li class="alert-empty">No hay productos por debajo del stock mínimo.</li>`;
  } else {
    listaAlertas.innerHTML = stockBajo.map((p) => `
      <li class="alert-item">
        <span>${p.nombre}</span>
        <strong>${p.stock} / ${p.stockMinimo} ${p.unidad}</strong>
      </li>`).join("");
  }
}

/* ----------------------------------------------------------------------- */
/* INVENTARIO: tabla, filtros, acciones                                    */
/* ----------------------------------------------------------------------- */

function inicializarToolbarInventario() {
  document.getElementById("buscador").addEventListener("input", renderTablaInventario);
  document.getElementById("filtro-categoria").addEventListener("change", renderTablaInventario);
  document.getElementById("filtro-estado").addEventListener("change", renderTablaInventario);
}

function renderTablaInventario() {
  const texto = document.getElementById("buscador").value.trim().toLowerCase();
  const categoria = document.getElementById("filtro-categoria").value;
  const estado = document.getElementById("filtro-estado").value;

  let productos = ProductosAPI.listar();

  if (texto) {
    productos = productos.filter((p) => p.nombre.toLowerCase().includes(texto));
  }
  if (categoria !== "todas") {
    productos = productos.filter((p) => p.categoria === categoria);
  }
  if (estado !== "todos") {
    productos = productos.filter((p) =>
      estado === "bajo" ? p.stock < p.stockMinimo : p.stock >= p.stockMinimo
    );
  }

  const tbody = document.getElementById("tbody-inventario");
  const tablaVacia = document.getElementById("tabla-vacia");

  if (productos.length === 0) {
    tbody.innerHTML = "";
    tablaVacia.hidden = false;
    return;
  }
  tablaVacia.hidden = true;

  tbody.innerHTML = productos.map((p) => {
    const bajo = p.stock < p.stockMinimo;
    return `
      <tr>
        <td>${p.nombre}</td>
        <td>${p.categoria}</td>
        <td>${p.unidad}</td>
        <td>${p.stock}</td>
        <td>${p.stockMinimo}</td>
        <td>${formatearMoneda(p.precio)}</td>
        <td><span class="badge ${bajo ? "badge-bajo" : "badge-ok"}">${bajo ? "Stock bajo" : "Normal"}</span></td>
        <td class="row-actions">
          <button class="icon-btn" data-editar="${p.id}">Editar</button>
        </td>
      </tr>`;
  }).join("");

  // Delegación de eventos: se asignan los listeners tras pintar las filas
  tbody.querySelectorAll("[data-editar]").forEach((btn) => {
    btn.addEventListener("click", () => abrirModalProducto(btn.dataset.editar));
  });
}

/* ----------------------------------------------------------------------- */
/* MODAL: crear / editar producto                                          */
/* ----------------------------------------------------------------------- */

function inicializarModalProducto() {
  const overlay = document.getElementById("modal-overlay");
  const form = document.getElementById("form-producto");

  document.getElementById("btn-nuevo-producto").addEventListener("click", () => abrirModalProducto(null));
  document.getElementById("btn-nuevo-producto-dash").addEventListener("click", () => abrirModalProducto(null));
  document.getElementById("btn-cerrar-modal").addEventListener("click", cerrarModalProducto);
  document.getElementById("btn-cancelar-modal").addEventListener("click", cerrarModalProducto);

  overlay.addEventListener("click", (e) => {
    if (e.target === overlay) cerrarModalProducto();
  });

  // Cerrar también con la tecla Escape (accesibilidad y robustez)
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && !overlay.hidden) cerrarModalProducto();
  });

  form.addEventListener("submit", (e) => {
    e.preventDefault();
    guardarProductoDesdeFormulario();
  });

  document.getElementById("btn-eliminar-producto").addEventListener("click", () => {
    const id = document.getElementById("prod-id").value;
    if (!id) return;
    if (confirm("¿Eliminar este producto del inventario? Esta acción no se puede deshacer.")) {
      ProductosAPI.eliminar(id);
      cerrarModalProducto();
      renderTodo();
      mostrarToast("Producto eliminado");
    }
  });
}

function abrirModalProducto(id) {
  const overlay = document.getElementById("modal-overlay");
  const titulo = document.getElementById("modal-titulo");
  const btnEliminar = document.getElementById("btn-eliminar-producto");

  document.getElementById("form-producto").reset();

  if (id) {
    const producto = ProductosAPI.obtener(id);
    titulo.textContent = "Editar producto";
    btnEliminar.hidden = false;
    document.getElementById("prod-id").value = producto.id;
    document.getElementById("prod-nombre").value = producto.nombre;
    document.getElementById("prod-categoria").value = producto.categoria;
    document.getElementById("prod-unidad").value = producto.unidad;
    document.getElementById("prod-stock").value = producto.stock;
    document.getElementById("prod-stock-min").value = producto.stockMinimo;
    document.getElementById("prod-precio").value = producto.precio;
    document.getElementById("prod-proveedor").value = producto.proveedor || "";
  } else {
    titulo.textContent = "Nuevo producto";
    btnEliminar.hidden = true;
    document.getElementById("prod-id").value = "";
  }

  overlay.hidden = false;
}

function cerrarModalProducto() {
  document.getElementById("modal-overlay").hidden = true;
}

function guardarProductoDesdeFormulario() {
  const id = document.getElementById("prod-id").value;

  const datos = {
    nombre: document.getElementById("prod-nombre").value.trim(),
    categoria: document.getElementById("prod-categoria").value,
    unidad: document.getElementById("prod-unidad").value.trim(),
    stock: Number(document.getElementById("prod-stock").value),
    stockMinimo: Number(document.getElementById("prod-stock-min").value),
    precio: Number(document.getElementById("prod-precio").value),
    proveedor: document.getElementById("prod-proveedor").value.trim(),
  };

  if (id) {
    ProductosAPI.actualizar(id, datos);
    mostrarToast("Producto actualizado");
  } else {
    ProductosAPI.crear(datos);
    mostrarToast("Producto creado");
  }

  cerrarModalProducto();
  renderTodo();
}

/* ----------------------------------------------------------------------- */
/* MOVIMIENTOS                                                             */
/* ----------------------------------------------------------------------- */

function renderSelectProductosMovimiento() {
  const select = document.getElementById("mov-producto");
  const seleccionActual = select.value;
  const productos = ProductosAPI.listar();

  select.innerHTML = productos.map((p) =>
    `<option value="${p.id}">${p.nombre} (stock: ${p.stock} ${p.unidad})</option>`
  ).join("");

  if (seleccionActual) select.value = seleccionActual;
}

function inicializarFormularioMovimiento() {
  document.getElementById("form-movimiento").addEventListener("submit", (e) => {
    e.preventDefault();

    const productoId = document.getElementById("mov-producto").value;
    const tipo = document.getElementById("mov-tipo").value;
    const cantidad = Number(document.getElementById("mov-cantidad").value);
    const motivo = document.getElementById("mov-motivo").value.trim();

    if (!productoId || cantidad <= 0) {
      mostrarToast("Verifica el producto y la cantidad");
      return;
    }

    // Regla de negocio: no permitir salidas mayores al stock disponible
    const producto = ProductosAPI.obtener(productoId);
    if (tipo === "salida" && cantidad > producto.stock) {
      mostrarToast(`Stock insuficiente: solo hay ${producto.stock} ${producto.unidad}`);
      return;
    }

    MovimientosAPI.registrar({ productoId, tipo, cantidad, motivo });
    e.target.reset();
    renderTodo();
    mostrarToast("Movimiento registrado");
  });
}

function renderHistorialMovimientos() {
  const movimientos = MovimientosAPI.listar().slice(0, 25); // últimos 25
  const tbody = document.getElementById("tbody-movimientos");

  if (movimientos.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5" class="tabla-vacia">Aún no se han registrado movimientos.</td></tr>`;
    return;
  }

  tbody.innerHTML = movimientos.map((m) => `
    <tr>
      <td>${formatearFecha(m.fecha)}</td>
      <td>${m.productoNombre}</td>
      <td><span class="badge ${m.tipo === "entrada" ? "badge-ok" : "badge-bajo"}">${m.tipo}</span></td>
      <td>${m.cantidad}</td>
      <td>${m.motivo || "—"}</td>
    </tr>`).join("");
}

/* ----------------------------------------------------------------------- */
/* REPORTES                                                                */
/* ----------------------------------------------------------------------- */

function renderResumenReporte() {
  const productos = ProductosAPI.listar();
  const stockBajo = productos.filter((p) => p.stock < p.stockMinimo);
  const valorInventario = productos.reduce((acc, p) => acc + p.stock * p.precio, 0);

  const parrafo = document.getElementById("resumen-texto");
  parrafo.textContent =
    `El inventario cuenta con ${productos.length} productos activos, con un valor total ` +
    `estimado de ${formatearMoneda(valorInventario)}. Actualmente ${stockBajo.length} producto(s) ` +
    `se encuentran por debajo del stock mínimo definido` +
    (stockBajo.length ? `: ${stockBajo.map((p) => p.nombre).join(", ")}.` : ".");
}

function inicializarExportarCSV() {
  document.getElementById("btn-exportar-csv").addEventListener("click", () => {
    const productos = ProductosAPI.listar();
    const encabezado = ["Nombre", "Categoria", "Unidad", "Stock", "StockMinimo", "Precio", "Proveedor"];
    const filas = productos.map((p) =>
      [p.nombre, p.categoria, p.unidad, p.stock, p.stockMinimo, p.precio, p.proveedor || ""]
        .map((v) => `"${String(v).replace(/"/g, '""')}"`).join(",")
    );
    const csv = [encabezado.join(","), ...filas].join("\n");

    const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);
    const enlace = document.createElement("a");
    enlace.href = url;
    enlace.download = "raspicholados_inventario.csv";
    enlace.click();
    URL.revokeObjectURL(url);
  });
}
