package co.sena.raspicholados.api.repositorio.memoria;

import co.sena.raspicholados.api.modelo.Producto;
import co.sena.raspicholados.api.modelo.Proveedor;
import co.sena.raspicholados.api.repositorio.ProveedorRepositorio;

import java.util.ArrayList;
import java.util.List;

/** Implementación en memoria del repositorio de proveedores. */
public class ProveedorRepositorioMemoria implements ProveedorRepositorio {

    private final BaseDatosMemoria bd;

    public ProveedorRepositorioMemoria(BaseDatosMemoria bd) {
        this.bd = bd;
    }

    @Override
    public List<Proveedor> listar() {
        synchronized (bd.CANDADO) {
            List<Proveedor> lista = new ArrayList<Proveedor>();
            for (Proveedor p : bd.proveedores.values()) {
                lista.add(p.copia());
            }
            return lista;
        }
    }

    @Override
    public Proveedor buscarPorId(long id) {
        synchronized (bd.CANDADO) {
            Proveedor p = bd.proveedores.get(id);
            return p == null ? null : p.copia();
        }
    }

    @Override
    public Proveedor buscarPorNit(String nit) {
        synchronized (bd.CANDADO) {
            for (Proveedor p : bd.proveedores.values()) {
                if (p.getNit().equalsIgnoreCase(nit)) {
                    return p.copia();
                }
            }
            return null;
        }
    }

    @Override
    public Proveedor guardar(Proveedor proveedor) {
        synchronized (bd.CANDADO) {
            Proveedor nuevo = proveedor.copia();
            nuevo.setId(++bd.secuenciaProveedor);
            bd.proveedores.put(nuevo.getId(), nuevo);
            return nuevo.copia();
        }
    }

    @Override
    public Proveedor actualizar(Proveedor proveedor) {
        synchronized (bd.CANDADO) {
            bd.proveedores.put(proveedor.getId(), proveedor.copia());
            return proveedor.copia();
        }
    }

    @Override
    public void eliminar(long id) {
        synchronized (bd.CANDADO) {
            bd.proveedores.remove(id);
            // Equivalente a ON DELETE SET NULL en los productos inactivos.
            for (Producto p : bd.productos.values()) {
                if (p.getProveedorId() != null && p.getProveedorId() == id) {
                    p.setProveedorId(null);
                }
            }
        }
    }

    @Override
    public int contarProductosActivos(long proveedorId) {
        synchronized (bd.CANDADO) {
            int total = 0;
            for (Producto p : bd.productos.values()) {
                if (p.isActivo() && p.getProveedorId() != null && p.getProveedorId() == proveedorId) {
                    total++;
                }
            }
            return total;
        }
    }
}
