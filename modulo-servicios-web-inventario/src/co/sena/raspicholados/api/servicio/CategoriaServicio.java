package co.sena.raspicholados.api.servicio;

import co.sena.raspicholados.api.http.ApiException;
import co.sena.raspicholados.api.modelo.Categoria;
import co.sena.raspicholados.api.repositorio.CategoriaRepositorio;

import java.util.List;
import java.util.Map;

/** Reglas de negocio de las categorías de productos. */
public class CategoriaServicio {

    private final CategoriaRepositorio categorias;

    public CategoriaServicio(CategoriaRepositorio categorias) {
        this.categorias = categorias;
    }

    public List<Categoria> listar() {
        return categorias.listar();
    }

    public Categoria obtener(long id) {
        Categoria c = categorias.buscarPorId(id);
        if (c == null) {
            throw ApiException.noEncontrado("una categoría", id);
        }
        return c;
    }

    public Categoria crear(Map<String, Object> datos) {
        Categoria c = leer(datos);
        if (categorias.buscarPorNombre(c.getNombre()) != null) {
            throw duplicada(c.getNombre());
        }
        return categorias.guardar(c);
    }

    public Categoria actualizar(long id, Map<String, Object> datos) {
        obtener(id);
        Categoria c = leer(datos);
        Categoria mismoNombre = categorias.buscarPorNombre(c.getNombre());
        if (mismoNombre != null && mismoNombre.getId() != id) {
            throw duplicada(c.getNombre());
        }
        c.setId(id);
        return categorias.actualizar(c);
    }

    public void eliminar(long id) {
        Categoria c = obtener(id);
        int productos = categorias.contarProductos(c.getNombre());
        if (productos > 0) {
            throw ApiException.conflicto("CATEGORIA_CON_PRODUCTOS",
                    "No se puede eliminar la categoría '" + c.getNombre() + "' porque tiene " + productos
                            + " producto(s) asociado(s) (incluye productos inactivos). Reasígnelos primero.");
        }
        categorias.eliminar(id);
    }

    private static Categoria leer(Map<String, Object> datos) {
        Validador v = new Validador(datos);
        Categoria c = new Categoria();
        c.setNombre(v.texto("nombre", true, 2, 50));
        c.setDescripcion(v.texto("descripcion", false, 0, 200));
        v.validar();
        return c;
    }

    private static ApiException duplicada(String nombre) {
        return ApiException.conflicto("CATEGORIA_DUPLICADA", "Ya existe una categoría llamada '" + nombre + "'");
    }
}
