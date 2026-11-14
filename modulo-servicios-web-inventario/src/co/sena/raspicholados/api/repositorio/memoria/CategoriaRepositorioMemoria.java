package co.sena.raspicholados.api.repositorio.memoria;

import co.sena.raspicholados.api.modelo.Categoria;
import co.sena.raspicholados.api.modelo.Producto;
import co.sena.raspicholados.api.repositorio.CategoriaRepositorio;

import java.util.ArrayList;
import java.util.List;

/** Implementación en memoria del repositorio de categorías. */
public class CategoriaRepositorioMemoria implements CategoriaRepositorio {

    private final BaseDatosMemoria bd;

    public CategoriaRepositorioMemoria(BaseDatosMemoria bd) {
        this.bd = bd;
    }

    @Override
    public List<Categoria> listar() {
        synchronized (bd.CANDADO) {
            List<Categoria> lista = new ArrayList<Categoria>();
            for (Categoria c : bd.categorias.values()) {
                lista.add(conConteo(c));
            }
            return lista;
        }
    }

    @Override
    public Categoria buscarPorId(long id) {
        synchronized (bd.CANDADO) {
            Categoria c = bd.categorias.get(id);
            return c == null ? null : conConteo(c);
        }
    }

    @Override
    public Categoria buscarPorNombre(String nombre) {
        synchronized (bd.CANDADO) {
            for (Categoria c : bd.categorias.values()) {
                if (c.getNombre().equalsIgnoreCase(nombre)) {
                    return conConteo(c);
                }
            }
            return null;
        }
    }

    @Override
    public Categoria guardar(Categoria categoria) {
        synchronized (bd.CANDADO) {
            Categoria nueva = categoria.copia();
            nueva.setId(++bd.secuenciaCategoria);
            bd.categorias.put(nueva.getId(), nueva);
            return conConteo(nueva);
        }
    }

    @Override
    public Categoria actualizar(Categoria categoria) {
        synchronized (bd.CANDADO) {
            Categoria actual = bd.categorias.get(categoria.getId());
            // Equivalente a ON UPDATE CASCADE de la llave foránea en MySQL.
            for (Producto p : bd.productos.values()) {
                if (p.getCategoria().equals(actual.getNombre())) {
                    p.setCategoria(categoria.getNombre());
                }
            }
            actual.setNombre(categoria.getNombre());
            actual.setDescripcion(categoria.getDescripcion());
            return conConteo(actual);
        }
    }

    @Override
    public void eliminar(long id) {
        synchronized (bd.CANDADO) {
            bd.categorias.remove(id);
        }
    }

    @Override
    public int contarProductos(String nombreCategoria) {
        synchronized (bd.CANDADO) {
            int total = 0;
            for (Producto p : bd.productos.values()) {
                if (p.getCategoria().equals(nombreCategoria)) {
                    total++;
                }
            }
            return total;
        }
    }

    private Categoria conConteo(Categoria original) {
        Categoria c = original.copia();
        int activos = 0;
        for (Producto p : bd.productos.values()) {
            if (p.isActivo() && p.getCategoria().equals(c.getNombre())) {
                activos++;
            }
        }
        c.setCantidadProductos(activos);
        return c;
    }
}
