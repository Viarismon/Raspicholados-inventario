package co.sena.raspicholados.api.repositorio;

import co.sena.raspicholados.api.modelo.Producto;

import java.util.List;

/** Contrato de persistencia de productos. */
public interface ProductoRepositorio {

    /** Lista solo productos activos que cumplan el filtro. */
    List<Producto> listar(FiltroProductos filtro);

    /** Busca por id (incluye inactivos; el servicio decide qué hacer con ellos). */
    Producto buscarPorId(long id);

    /** Producto activo con el mismo nombre (sin distinguir mayúsculas), o null. */
    Producto buscarActivoPorNombre(String nombre);

    /** Guarda un producto nuevo con stock 0; el stock inicial entra como movimiento. */
    Producto guardar(Producto producto);

    /** Actualiza los datos descriptivos. El stock NO se modifica aquí. */
    Producto actualizar(Producto producto);

    /** Eliminación lógica (activo = false) para conservar el historial de movimientos. */
    void desactivar(long id);
}
