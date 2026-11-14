package co.sena.raspicholados.api.repositorio;

import co.sena.raspicholados.api.modelo.Categoria;

import java.util.List;

/** Contrato de persistencia de categorías. */
public interface CategoriaRepositorio {

    List<Categoria> listar();

    Categoria buscarPorId(long id);

    /** Búsqueda sin distinguir mayúsculas de minúsculas. */
    Categoria buscarPorNombre(String nombre);

    Categoria guardar(Categoria categoria);

    /** Actualiza; si cambia el nombre, los productos asociados se actualizan en cascada. */
    Categoria actualizar(Categoria categoria);

    void eliminar(long id);

    /** Productos (activos e inactivos) que referencian la categoría. */
    int contarProductos(String nombreCategoria);
}
