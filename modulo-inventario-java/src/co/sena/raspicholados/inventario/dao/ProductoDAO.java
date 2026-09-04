package co.sena.raspicholados.inventario.dao;

import co.sena.raspicholados.inventario.modelo.Producto;
import java.util.List;

/**
 * Módulo: Inventario
 * Propósito: Contrato de acceso a datos para la entidad Producto.
 *            Define las operaciones de inserción, consulta,
 *            actualización y eliminación exigidas por la evidencia
 *            GA7-220501096-AA2-EV01.
 * Autora: Viviana Arias Montilla
 * Fecha de creación: 2026-08-28
 */
public interface ProductoDAO {

    boolean insertarProducto(Producto producto);

    Producto consultarProductoPorId(int idProducto);

    List<Producto> listarProductos();

    boolean actualizarProducto(Producto producto);

    boolean eliminarProducto(int idProducto);
}
