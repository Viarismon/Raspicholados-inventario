package co.sena.raspicholados.inventario.principal;

import co.sena.raspicholados.inventario.dao.ProductoDAO;
import co.sena.raspicholados.inventario.dao.ProductoDAOImpl;
import co.sena.raspicholados.inventario.modelo.Producto;

import java.util.List;

/**
 * Módulo: Inventario
 * Propósito: Clase principal que ejecuta y evidencia las cuatro
 *            operaciones CRUD (insertar, consultar, actualizar,
 *            eliminar) sobre el módulo de inventario, exigidas por
 *            la evidencia GA7-220501096-AA2-EV01.
 * Autora: Viviana Arias Montilla
 * Fecha de creación: 2026-08-28
 */
public class PruebaInventarioCRUD {

    public static void main(String[] argumentos) {

        ProductoDAO productoDAO = new ProductoDAOImpl();

        System.out.println("========== 1. INSERTAR PRODUCTO ==========");
        Producto productoNuevo = new Producto("Sirope de mora", "Insumo", "Botella", 3, 5);
        boolean insercionExitosa = productoDAO.insertarProducto(productoNuevo);
        System.out.println(insercionExitosa
                ? "Producto insertado correctamente."
                : "No fue posible insertar el producto.");

        System.out.println("\n========== 2. LISTAR PRODUCTOS (CONSULTAR) ==========");
        List<Producto> productos = productoDAO.listarProductos();
        for (Producto productoActual : productos) {
            String alerta = productoActual.tieneStockBajo() ? "  <-- STOCK BAJO" : "";
            System.out.println(productoActual + alerta);
        }

        if (!productos.isEmpty()) {
            int idProductoPrueba = productos.get(0).getIdProducto();

            System.out.println("\n========== 3. CONSULTAR PRODUCTO POR ID ==========");
            Producto productoConsultado = productoDAO.consultarProductoPorId(idProductoPrueba);
            System.out.println(productoConsultado != null
                    ? productoConsultado
                    : "Producto no encontrado.");

            System.out.println("\n========== 4. ACTUALIZAR PRODUCTO ==========");
            if (productoConsultado != null) {
                productoConsultado.setStockActual(productoConsultado.getStockActual() + 10);
                boolean actualizacionExitosa = productoDAO.actualizarProducto(productoConsultado);
                System.out.println(actualizacionExitosa
                        ? "Stock actualizado correctamente: " + productoConsultado
                        : "No fue posible actualizar el producto.");
            }

            System.out.println("\n========== 5. ELIMINAR PRODUCTO ==========");
            boolean eliminacionExitosa = productoDAO.eliminarProducto(idProductoPrueba);
            System.out.println(eliminacionExitosa
                    ? "Producto eliminado correctamente (id " + idProductoPrueba + ")."
                    : "No fue posible eliminar el producto.");
        }
    }
}
