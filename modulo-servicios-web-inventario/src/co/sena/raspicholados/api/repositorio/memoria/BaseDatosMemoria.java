package co.sena.raspicholados.api.repositorio.memoria;

import co.sena.raspicholados.api.modelo.Categoria;
import co.sena.raspicholados.api.modelo.Movimiento;
import co.sena.raspicholados.api.modelo.Producto;
import co.sena.raspicholados.api.modelo.Proveedor;
import co.sena.raspicholados.api.modelo.Usuario;
import co.sena.raspicholados.api.util.Fechas;

import java.math.BigDecimal;
import java.util.TreeMap;

/**
 * Almacén en memoria compartido por los repositorios del modo "memoria".
 *
 * Se usa cuando no hay MySQL disponible (por ejemplo, para una demostración
 * rápida o para las pruebas con Postman en un equipo sin base de datos).
 * Todos los accesos se sincronizan sobre el objeto {@link #CANDADO} para
 * que las operaciones compuestas (como registrar un movimiento) sean
 * atómicas. Los datos se pierden al detener el servidor.
 *
 * Al crearse, carga los mismos datos de ejemplo que el script SQL.
 */
public class BaseDatosMemoria {

    final Object CANDADO = new Object();

    final TreeMap<Long, Usuario> usuarios = new TreeMap<Long, Usuario>();
    final TreeMap<Long, Categoria> categorias = new TreeMap<Long, Categoria>();
    final TreeMap<Long, Proveedor> proveedores = new TreeMap<Long, Proveedor>();
    final TreeMap<Long, Producto> productos = new TreeMap<Long, Producto>();
    final TreeMap<Long, Movimiento> movimientos = new TreeMap<Long, Movimiento>();

    long secuenciaUsuario;
    long secuenciaCategoria;
    long secuenciaProveedor;
    long secuenciaProducto;
    long secuenciaMovimiento;

    public BaseDatosMemoria() {
        cargarDatosDeEjemplo();
    }

    private void cargarDatosDeEjemplo() {
        categoria("Raspados", "Raspados de hielo con jarabe de fruta");
        categoria("Helados", "Helados por litro para servir en copa o cono");
        categoria("Toppings", "Coberturas y adiciones");
        categoria("Insumos", "Materia prima para preparar los productos");
        categoria("Empaques", "Vasos, cucharas y desechables");

        proveedor("900123456-1", "Frutas del Valle S.A.S.", "3104567890", "ventas@frutasdelvalle.co", "Cra 5 # 12-30, Cali");
        proveedor("800765432-9", "Lácteos San Rafael", "3157654321", "pedidos@lacteossanrafael.co", "Cl 25 # 8-14, Palmira");
        proveedor("901555222-3", "Distribuidora Central", "6024441122", "contacto@distcentral.co", "Av 3N # 45-10, Cali");
        proveedor("901888777-5", "Empaques del Sur", "3201112233", "info@empaquesdelsur.co", "Cl 70 # 1-50, Cali");

        producto("Hielo raspado", "Insumos", "Kilogramo", 50, 10, "800", 3L);
        producto("Leche condensada", "Toppings", "Unidad", 20, 8, "6200", 3L);
        producto("Cereza en almíbar", "Toppings", "Frasco", 15, 5, "9500", 3L);
        producto("Jarabe de mora", "Raspados", "Litro", 12, 4, "14000", 1L);
        producto("Jarabe de mango", "Raspados", "Litro", 3, 4, "14000", 1L);
        producto("Helado de vainilla", "Helados", "Litro", 12, 5, "18000", 2L);
        producto("Helado de chocolate", "Helados", "Litro", 0, 5, "18500", 2L);
        producto("Vaso cholado grande", "Empaques", "Unidad", 120, 30, "350", 4L);
        producto("Cucharas plásticas", "Empaques", "Unidad", 90, 100, "60", 4L);
    }

    private void categoria(String nombre, String descripcion) {
        Categoria c = new Categoria();
        c.setId(++secuenciaCategoria);
        c.setNombre(nombre);
        c.setDescripcion(descripcion);
        categorias.put(c.getId(), c);
    }

    private void proveedor(String nit, String nombre, String telefono, String correo, String direccion) {
        Proveedor p = new Proveedor();
        p.setId(++secuenciaProveedor);
        p.setNit(nit);
        p.setNombre(nombre);
        p.setTelefono(telefono);
        p.setCorreo(correo);
        p.setDireccion(direccion);
        proveedores.put(p.getId(), p);
    }

    private void producto(String nombre, String categoria, String unidad, int stock, int minimo,
                          String precio, Long proveedorId) {
        Producto p = new Producto();
        p.setId(++secuenciaProducto);
        p.setNombre(nombre);
        p.setCategoria(categoria);
        p.setUnidadMedida(unidad);
        p.setStockActual(stock);
        p.setStockMinimo(minimo);
        p.setPrecioUnitario(new BigDecimal(precio));
        p.setProveedorId(proveedorId);
        p.setFechaRegistro(Fechas.ahora());
        productos.put(p.getId(), p);
    }

    /** Completa el nombre del proveedor (equivale al JOIN de la versión MySQL). */
    Producto conProveedor(Producto original) {
        Producto p = original.copia();
        if (p.getProveedorId() != null) {
            Proveedor prov = proveedores.get(p.getProveedorId());
            p.setProveedorNombre(prov == null ? null : prov.getNombre());
        }
        return p;
    }
}
