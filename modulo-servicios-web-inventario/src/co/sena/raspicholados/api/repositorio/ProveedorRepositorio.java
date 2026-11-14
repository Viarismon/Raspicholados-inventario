package co.sena.raspicholados.api.repositorio;

import co.sena.raspicholados.api.modelo.Proveedor;

import java.util.List;

/** Contrato de persistencia de proveedores. */
public interface ProveedorRepositorio {

    List<Proveedor> listar();

    Proveedor buscarPorId(long id);

    Proveedor buscarPorNit(String nit);

    Proveedor guardar(Proveedor proveedor);

    Proveedor actualizar(Proveedor proveedor);

    void eliminar(long id);

    /** Productos activos que tienen asignado el proveedor. */
    int contarProductosActivos(long proveedorId);
}
