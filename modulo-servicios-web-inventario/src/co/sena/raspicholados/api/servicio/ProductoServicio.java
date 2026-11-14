package co.sena.raspicholados.api.servicio;

import co.sena.raspicholados.api.http.ApiException;
import co.sena.raspicholados.api.modelo.Categoria;
import co.sena.raspicholados.api.modelo.Movimiento;
import co.sena.raspicholados.api.modelo.Producto;
import co.sena.raspicholados.api.repositorio.CategoriaRepositorio;
import co.sena.raspicholados.api.repositorio.FiltroProductos;
import co.sena.raspicholados.api.repositorio.ProductoRepositorio;
import co.sena.raspicholados.api.repositorio.ProveedorRepositorio;
import co.sena.raspicholados.api.seguridad.UsuarioAutenticado;

import java.util.List;
import java.util.Map;

/**
 * Reglas de negocio de los productos del inventario.
 *
 *  - El nombre es único entre los productos activos.
 *  - La categoría y el proveedor deben existir.
 *  - El stock NO se edita directamente: se modifica solo con movimientos
 *    (ENTRADA, SALIDA, AJUSTE) para que todo cambio quede trazado. Al
 *    crear un producto se puede indicar "stockInicial", que se registra
 *    como un movimiento de ENTRADA "Inventario inicial".
 *  - Eliminar es una baja lógica (activo = false) para conservar el
 *    historial de movimientos del producto.
 */
public class ProductoServicio {

    private final ProductoRepositorio productos;
    private final CategoriaRepositorio categorias;
    private final ProveedorRepositorio proveedores;
    private final MovimientoServicio movimientos;

    public ProductoServicio(ProductoRepositorio productos, CategoriaRepositorio categorias,
                            ProveedorRepositorio proveedores, MovimientoServicio movimientos) {
        this.productos = productos;
        this.categorias = categorias;
        this.proveedores = proveedores;
        this.movimientos = movimientos;
    }

    public List<Producto> listar(FiltroProductos filtro) {
        return productos.listar(filtro);
    }

    public Producto obtener(long id) {
        Producto p = productos.buscarPorId(id);
        if (p == null || !p.isActivo()) {
            throw ApiException.noEncontrado("un producto activo", id);
        }
        return p;
    }

    public Producto crear(Map<String, Object> datos, UsuarioAutenticado usuario) {
        Validador v = new Validador(datos);
        Producto p = leer(v);
        Integer stockInicial = v.entero("stockInicial", false, 0, 1000000);
        if (v.contiene("stockActual")) {
            v.error("stockActual", "No se envía al crear. Use 'stockInicial'");
        }
        v.validar();
        if (productos.buscarActivoPorNombre(p.getNombre()) != null) {
            throw duplicado(p.getNombre());
        }
        Producto creado = productos.guardar(p);
        if (stockInicial != null && stockInicial > 0) {
            movimientos.registrarInterno(creado.getId(), Movimiento.ENTRADA, stockInicial,
                    "Inventario inicial", usuario);
            creado = productos.buscarPorId(creado.getId());
        }
        return creado;
    }

    public Producto actualizar(long id, Map<String, Object> datos) {
        obtener(id);
        Validador v = new Validador(datos);
        Producto p = leer(v);
        if (v.contiene("stockActual") || v.contiene("stockInicial")) {
            v.error("stockActual", "El stock no se modifica aquí. Registre un movimiento en POST /api/movimientos");
        }
        v.validar();
        Producto mismoNombre = productos.buscarActivoPorNombre(p.getNombre());
        if (mismoNombre != null && mismoNombre.getId() != id) {
            throw duplicado(p.getNombre());
        }
        p.setId(id);
        return productos.actualizar(p);
    }

    public void eliminar(long id) {
        obtener(id);
        productos.desactivar(id);
    }

    /** Lee y valida los campos comunes a crear y actualizar. */
    private Producto leer(Validador v) {
        Producto p = new Producto();
        p.setNombre(v.texto("nombre", true, 2, 80));
        String nombreCategoria = v.texto("categoria", true, 2, 50);
        p.setUnidadMedida(v.texto("unidadMedida", true, 1, 20));
        Integer minimo = v.entero("stockMinimo", true, 0, 1000000);
        p.setStockMinimo(minimo == null ? 0 : minimo);
        p.setPrecioUnitario(v.dinero("precioUnitario", true));
        p.setProveedorId(v.id("proveedorId", false));

        if (nombreCategoria != null) {
            Categoria categoria = categorias.buscarPorNombre(nombreCategoria);
            if (categoria == null) {
                v.error("categoria", "La categoría '" + nombreCategoria + "' no existe. Consulte GET /api/categorias");
            } else {
                p.setCategoria(categoria.getNombre()); // nombre con las mayúsculas oficiales
            }
        }
        if (p.getProveedorId() != null && proveedores.buscarPorId(p.getProveedorId()) == null) {
            v.error("proveedorId", "No existe un proveedor con id " + p.getProveedorId());
        }
        return p;
    }

    private static ApiException duplicado(String nombre) {
        return ApiException.conflicto("PRODUCTO_DUPLICADO", "Ya existe un producto activo llamado '" + nombre + "'");
    }
}
